# WebSocket Implementation Explanation

This document explains how the WebSocket real-time comment feature was implemented across the microservices.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        Browser                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │            websocket_test.html (Port 8080)               │   │
│  │  Section 1: Connect ──┐                                  │   │
│  │  Section 2: Send Comment                                     │   │
│  │  Section 3: Live Feed  │                                  │   │
│  └────────────┬───────────┘                                  │   │
│               │                                                │   │
│    ┌──────────┴──────────┐                                   │   │
│    │ REST: http://8080   │ WebSocket: ws://8083              │   │
└────┼─────────────────────┼───────────────────────────────────┘   │
     │                     │
     │                     │
┌────▼─────────────────────▼───────────────────────────────────┐
│                    API Gateway (Port 8080)                     │
│  Routes: /users/**, /posts/**, /comments/**, /ws/comments/**  │
└───────────────────────────┬───────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────┐
│              Comment Service (Port 8083)                       │
│  ┌─────────────────────┐    ┌─────────────────────────────┐   │
│  │  WebSocket Server   │───▶│ CommentWebSocketHandler     │   │
│  │  /ws/comments/{id}  │    │ - Manages sessions per post │   │
│  └─────────────────────┘    │ - Broadcasts to subscribers │   │
│                             └─────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ CommentService.createComment()                              ││
│  │ - Saves comment to MongoDB                                  ││
│  │ - Sends Kafka event                                         ││
│  │ - Calls webSocketHandler.broadcastToPost()                 ││
│  └─────────────────────────────────────────────────────────────┘│
└────────────────────────────────────────────────────────────────┘
```

---

## 1. Backend - Comment Service

### 1.1 pom.xml - Dependencies

**File**: `comment-service/pom.xml`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

This dependency enables Spring's WebSocket support including:
- WebSocket endpoint registration
- WebSocket handler infrastructure
- Message handling

### 1.2 WebSocketConfig - Server Configuration

**File**: `comment-service/src/main/java/com/demo/comment/config/WebSocketConfig.java`

```java
package com.demo.comment.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.demo.comment.websocket.CommentWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private CommentWebSocketHandler commentWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(commentWebSocketHandler, "/ws/comments/{postId}")
                .setAllowedOrigins("http://localhost:8080");
    }
}
```

**Key points**:
- `@EnableWebSocket` - Enables Spring's WebSocket support
- `WebSocketConfigurer` - Allows custom handler registration
- `setAllowedOrigins("http://localhost:8080")` - **Critical for CORS** - allows browser connections from the API Gateway origin
- Path pattern `/ws/comments/{postId}` - The `{postId}` is a path variable that the handler extracts

### 1.3 CommentWebSocketHandler - WebSocket Logic

**File**: `comment-service/src/main/java/com/demo/comment/websocket/CommentWebSocketHandler.java`

```java
package com.demo.comment.websocket;

import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class CommentWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(CommentWebSocketHandler.class);

    // Map: postId -> Set of WebSocket sessions subscribed to that post
    private final java.util.Map<String, Set<WebSocketSession>> sessionsByPostId = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String postId = getPostId(session);
        if (postId == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        // Add session to the set of sessions for this postId
        sessionsByPostId.computeIfAbsent(postId, k -> new CopyOnWriteArraySet<>()).add(session);
        log.info("WebSocket connected for postId: {}", postId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String postId = getPostId(session);
        if (postId != null) {
            Set<WebSocketSession> sessions = sessionsByPostId.get(postId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    sessionsByPostId.remove(postId);
                }
            }
            log.info("WebSocket disconnected for postId: {}", postId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("Received message: {}", message.getPayload());
    }

    // Called by CommentService when a new comment is created
    public void broadcastToPost(String postId, String commentId, String userId, String content) {
        Set<WebSocketSession> sessions = sessionsByPostId.get(postId);
        if (sessions == null || sessions.isEmpty()) {
            return; // No clients subscribed to this post
        }

        // Create JSON message
        ObjectNode json = objectMapper.createObjectNode();
        json.put("commentId", commentId);
        json.put("postId", postId);
        json.put("userId", userId);
        json.put("content", content);
        json.put("timestamp", System.currentTimeMillis());

        String message = json.toString();
        
        // Broadcast to all connected clients subscribed to this postId
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (IOException e) {
                log.error("Error sending message to session", e);
            }
        }
    }

    // Extract postId from the WebSocket path (e.g., /ws/comments/abc123 -> abc123)
    private String getPostId(WebSocketSession session) {
        URI uri = session.getUri();
        String path = uri.getPath();
        if (path == null || path.isEmpty()) {
            return null;
        }
        String[] segments = path.split("/");
        if (segments.length == 0) {
            return null;
        }
        String lastSegment = segments[segments.length - 1];
        if (!lastSegment.matches("^[a-zA-Z0-9]+$")) {
            return null;
        }
        return lastSegment;
    }
}
```

**Key concepts**:
- `sessionsByPostId` - A ConcurrentHashMap that maps each postId to a set of WebSocket sessions
- `afterConnectionEstablished` - When a client connects, extract postId from URL path and register the session
- `afterConnectionClosed` - When a client disconnects, remove the session from the map
- `broadcastToPost` - Public method that other services (CommentService) call to send messages to all clients subscribed to a specific post

### 1.4 CommentService - Integration

**File**: `comment-service/src/main/java/com/demo/comment/service/CommentService.java`

```java
@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final UserClient userClient;
    private final PostClient postClient;
    private final CommentWebSocketHandler webSocketHandler;  // <-- Inject handler

    public Comment createComment(Comment comment) {
        // Validate user exists
        User user = userClient.getUser(comment.getUserId()).getBody();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");
        }
        
        // Validate post exists
        Post post = postClient.getPost(comment.getPostId()).getBody();
        if (post == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post not found");
        }
        
        // Save comment to MongoDB
        Comment saved = commentRepository.save(comment);
        
        // Send Kafka event
        kafkaTemplate.send("comment-events", "COMMENT_CREATED:" + saved.getId());
        
        // Broadcast to WebSocket clients subscribed to this post
        webSocketHandler.broadcastToPost(
            saved.getPostId(), 
            saved.getId(), 
            saved.getUserId(), 
            saved.getContent()
        );
        
        return saved;
    }
    // ... other methods
}
```

**Flow**:
1. Validate userId exists via UserClient (Feign)
2. Validate postId exists via PostClient (Feign)
3. Save comment to MongoDB
4. Send Kafka event (for notification service)
5. **Broadcast to WebSocket** - Call `webSocketHandler.broadcastToPost()` to notify all connected clients

---

## 2. API Gateway - Configuration

### 2.1 application.properties - Routes

**File**: `api-gateway/src/main/resources/application.properties`

```properties
server.port=8080
spring.application.name=api-gateway
spring.main.web-application-type=reactive

# Regular routes
spring.cloud.gateway.routes[0].id=user-service
spring.cloud.gateway.routes[0].uri=lb://user-service
spring.cloud.gateway.routes[0].predicates[0]=Path=/users/**

spring.cloud.gateway.routes[1].id=post-service
spring.cloud.gateway.routes[1].uri=lb://post-service
spring.cloud.gateway.routes[1].predicates[0]=Path=/posts/**

spring.cloud.gateway.routes[2].id=comment-service
spring.cloud.gateway.routes[2].uri=lb://comment-service
spring.cloud.gateway.routes[2].predicates[0]=Path=/comments/**

# WebSocket route
spring.cloud.gateway.routes[3].id=comment-service-ws
spring.cloud.gateway.routes[3].uri=lb://comment-service
spring.cloud.gateway.routes[3].predicates[0]=Path=/ws/comments/**
```

**Important notes**:
- WebSocket route uses `lb://comment-service` (load balancer)
- However, we discovered that browser WebSocket connections work better when connecting directly to the comment-service (port 8083), not through the gateway
- This is because Spring Cloud Gateway's WebSocket support has limitations with the reactive stack

---

## 3. Frontend - HTML Client

**File**: `api-gateway/src/main/resources/static/websocket_test.html`

### 3.1 HTML Structure

```html
<body>
    <!-- Section 1: Connect to Post -->
    <div class="section section-1">
        <h2>Section 1: Connect to Post</h2>
        <div class="input-row">
            <input type="text" id="postIdInput" placeholder="Enter postId">
            <button id="connectBtn">Connect</button>
        </div>
        <div class="status" id="connectionStatus"></div>
        <div class="field-row">
            <label>Post Title:</label>
            <div id="postTitle"></div>  <!-- Plain text view -->
        </div>
        <div class="field-row">
            <label>Post Content:</label>
            <div id="postContent"></div>  <!-- Plain text view -->
        </div>
    </div>
    
    <!-- Section 2: Send Comment -->
    <div class="section section-2">
        <h2>Section 2: Send Comment</h2>
        <div class="field-row">
            <label for="userIdInput">User ID:</label>
            <input type="text" id="userIdInput">
        </div>
        <div class="field-row">
            <label for="commentInput">Comment:</label>
            <input type="text" id="commentInput">
        </div>
        <button id="sendBtn">Send Comment</button>
        <div class="status" id="sendStatus"></div>
    </div>
    
    <!-- Section 3: Live Comments Feed -->
    <div class="section section-3">
        <h2>Section 3: Live Comments Feed</h2>
        <div class="comment-list" id="commentsContainer"></div>
        <button class="clear-btn" id="clearBtn">Clear Comments</button>
    </div>
</body>
```

### 3.2 CSS Layout

```css
body {
    display: flex;
    flex-direction: column;  /* Vertical - each section in own row */
    height: 100vh;
}

.section {
    flex: none;  /* Not flex growing - takes natural height */
    padding: 20px;
    border-bottom: 2px solid #ccc;
}

.section-1 { background-color: #e3f2fd; }  /* Light blue */
.section-2 { background-color: #e8f5e8; }  /* Light green */
.section-3 { background-color: #fff3e0; }  /* Light orange */

.comment-list {
    overflow-y: auto;  /* Scrollable */
    max-height: 300px;
}
```

### 3.3 JavaScript - Connection Logic

```javascript
let ws = null;
let currentPostId = null;

function connectToPost() {
    const postId = document.getElementById('postIdInput').value.trim();
    if (!postId) {
        showStatus('connectionStatus', 'Please enter a postId', 'error');
        return;
    }
    
    // Close existing connection if any
    if (ws) {
        ws.close();
        ws = null;
    }
    
    // Clear Section 3 comments (postId may have changed)
    document.getElementById('commentsContainer').innerHTML = '';
    
    // First, verify post exists via REST
    fetch(`http://localhost:8080/posts/${postId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Post not found');
            }
            return response.json();
        })
        .then(post => {
            // Display post details (plain text views)
            document.getElementById('postTitle').textContent = post.title || '';
            document.getElementById('postContent').textContent = post.content || '';
            currentPostId = postId;
            
            // Connect to WebSocket - DIRECTLY to comment-service (port 8083)
            // NOT through API Gateway (8080) due to WebSocket limitations
            const wsUrl = `ws://localhost:8083/ws/comments/${postId}`;
            ws = new WebSocket(wsUrl);
            
            ws.onopen = () => {
                showStatus('connectionStatus', 'Connected successfully!', 'success');
            };
            
            ws.onmessage = (event) => {
                // Parse JSON and add to feed
                const comment = JSON.parse(event.data);
                addCommentToFeed(comment);
            };
            
            ws.onerror = (event) => {
                showStatus('connectionStatus', 'WebSocket connection error', 'error');
                window.alert('WebSocket connection failed');
            };
            
            ws.onclose = (event) => {
                showStatus('connectionStatus', 'Disconnected', 'error');
                ws = null;
            };
        })
        .catch(error => {
            showStatus('connectionStatus', error.message, 'error');
        });
}
```

### 3.4 JavaScript - Send Comment

```javascript
function sendComment() {
    if (!currentPostId) {
        showStatus('sendStatus', 'Please connect to a post first', 'error');
        return;
    }
    
    const userId = document.getElementById('userIdInput').value.trim();
    const content = document.getElementById('commentInput').value.trim();
    
    // Send via REST API through gateway
    fetch('http://localhost:8080/comments', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            postId: currentPostId,
            userId: userId,
            content: content
        })
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to send comment');
        showStatus('sendStatus', 'Comment sent successfully!', 'success');
        document.getElementById('commentInput').value = '';
    })
    .catch(error => {
        showStatus('sendStatus', error.message, 'error');
    });
}
```

### 3.5 JavaScript - Live Feed Updates

```javascript
function addCommentToFeed(comment) {
    const container = document.getElementById('commentsContainer');
    const commentDiv = document.createElement('div');
    commentDiv.className = 'comment-item';
    commentDiv.innerHTML = `
        <div class="comment-meta">By: ${comment.userId || 'Unknown'} | ${new Date(comment.timestamp).toLocaleTimeString()}</div>
        <div class="comment-content">${comment.content}</div>
    `;
    container.appendChild(commentDiv);
    container.scrollTop = container.scrollHeight;  // Auto-scroll to bottom
}
```

---

## 4. Key Design Decisions

### Why Connect Directly to Port 8083?

1. Spring Cloud Gateway is built on the reactive stack (Netty)
2. WebSocket support in reactive Gateway has limitations
3. Browser WebSocket connections from `http://localhost:8080` to `ws://localhost:8083` work smoothly with proper CORS configuration
4. The WebSocket endpoint is at `/ws/comments/{postId}` on the comment-service

### Why CORS is Required

- Browser sends `Origin: http://localhost:8080` header
- Without `setAllowedOrigins("http://localhost:8080")`, the WebSocket handshake fails
- This is configured in `WebSocketConfig.java` on the server side

### How Real-Time Updates Work

1. User creates a comment via REST POST `/comments`
2. `CommentService.createComment()` saves to MongoDB
3. After saving, it calls `webSocketHandler.broadcastToPost(postId, ...)`
4. The handler looks up all WebSocket sessions subscribed to that postId
5. Sends JSON to each session via `session.sendMessage()`
6. Browser receives message in `ws.onmessage`, parses JSON, appends to DOM

---

## 5. Files Summary

| File | Purpose |
|------|---------|
| `comment-service/pom.xml` | Added spring-boot-starter-websocket |
| `comment-service/.../config/WebSocketConfig.java` | Server config, CORS, handler registration |
| `comment-service/.../websocket/CommentWebSocketHandler.java` | Connection management, broadcasting |
| `comment-service/.../service/CommentService.java` | Added WebSocket broadcast on comment creation |
| `api-gateway/.../application.properties` | Added WebSocket route (though not used directly) |
| `api-gateway/.../static/websocket_test.html` | Client UI with WebSocket client code |

---

## 6. Testing the Implementation

1. Start all services (Eureka, Gateway, User, Post, Comment, Notification)
2. Open `http://localhost:8080/websocket_test.html` in browser
3. Enter a valid postId and click Connect
4. Post title/content should display as plain text
5. Create a comment via REST or the UI (Section 2)
6. The comment should appear in real-time in Section 3
7. Open multiple browser tabs to test real-time sync