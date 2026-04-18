# WebSocket Implementation Plan

## Overview
This document outlines the implementation of a WebSocket server in the comment-service and a client HTML page for testing real-time comment updates.

## Components Implemented

### 1. Comment Service WebSocket Server
- **Dependency Added**: `spring-boot-starter-websocket` in comment-service/pom.xml
- **WebSocket Config**: `WebSocketConfig.java` - enables WebSocket at `/ws/comments/{postId}` with CORS support
- **WebSocket Handler**: `CommentWebSocketHandler.java` - manages connections per postId and broadcasts comments
- **Service Integration**: Modified `CommentService.java` to broadcast new comments via WebSocket
- **Broadcast Format**: JSON object with commentId, postId, userId, content, and timestamp

### 2. API Gateway
- **WebSocket Route**: Added route for `/ws/comments/**` to direct traffic to comment-service
- Uses `lb:ws://comment-service` for load-balanced WebSocket proxying through gateway

### 3. Client Test Page
- **HTML File**: `websocket_test.html` in api-gateway/src/main/resources/static/
- **UI Layout**: Three vertically stacked sections with color coding
  - Section 1 (Blue): Post connection interface
  - Section 2 (Green): Comment sending interface  
  - Section 3 (Orange): Live comments feed
- **Features**:
  - Connect to a post by postId (fetches post details via REST)
  - Establish WebSocket connection to receive real-time comments
  - Send new comments via REST API
  - Display incoming comments in real-time feed
  - Clear comments feed
  - Status indicators (success/error) with color coding

### 4. Documentation Updates
- Updated README.md with WebSocket test instructions

## Technical Details

### WebSocket Connection
- URL: `ws://localhost:8080/ws/comments/{postId}` (through API Gateway)
- Path variable `{postId}` determines which post's comments to receive
- Automatic cleanup of disconnected sessions
- CORS enabled for `http://localhost:8080` origin

### Message Format
Broadcast JSON from server to client:
```json
{
  "commentId": "generated-id",
  "postId": "referenced-post-id", 
  "userId": "commenter-user-id",
  "content": "comment-text",
  "timestamp": 1234567890
}
```

### Client-Side Behavior
1. **Connection Flow**:
   - User enters postId and clicks Connect
   - Client validates post exists via `GET /posts/{postId}`
   - If valid, connects to WebSocket endpoint at `ws://localhost:8080/ws/comments/{postId}`
   - On success: shows green status, displays post title/content as plain text views
   - On failure: shows red error status + window.alert with detailed error

2. **Comment Submission**:
   - User enters userId and comment content
   - Client sends POST to `/comments` through API Gateway
   - Shows success/error status based on response

3. **Real-Time Updates**:
   - When comment is created, service broadcasts to all WebSocket clients subscribed to that postId
   - Client receives message, parses JSON, appends to comments feed
   - Each comment displayed with user info and timestamp

4. **UI Management**:
   - Connect button remains enabled (allows reconnecting to different postId)
   - Section 3 comments list clears on Connect click (postId may have changed)
   - Auto-scroll to latest comment in feed
   - Clear button removes all comments from display

## Files Modified/Added

### comment-service
- pom.xml: Added `spring-boot-starter-websocket` dependency
- src/main/java/com/demo/comment/config/WebSocketConfig.java: NEW
- src/main/java/com/demo/comment/websocket/CommentWebSocketHandler.java: NEW
- src/main/java/com/demo/comment/service/CommentService.java: Modified (added websocket broadcast)

### api-gateway
- pom.xml: Initially added websocket but removed due to Spring Cloud Gateway incompatibility
- src/main/resources/application.properties: Added WebSocket route for `/ws/comments/**`
- src/main/resources/static/websocket_test.html: NEW

### Documentation
- PLAN_websocket.md: THIS FILE
- README.md: Updated with WebSocket test instructions

## Usage Instructions

1. Start all microservices as usual (Eureka, Gateway, User, Post, Comment, Notification)
2. Open browser to: `http://localhost:8080/websocket_test.html`
3. Enter a valid postId and click Connect
4. Enter userId and comment, click Send Comment
5. Observe real-time updates in the comments feed section
6. Open multiple browser tabs to test real-time sync between clients

## Architecture Update (2025)

- **WebSocket Route**: Changed from `lb://comment-service` to `lb:ws://comment-service` in API Gateway
- **Frontend**: Connects to `ws://localhost:8080/ws/comments/{postId}` through API Gateway
- This enables centralized routing through gateway for consistency with other services