package com.demo.comment.websocket;

import java.io.IOException;
import java.net.URI;
import java.util.List;
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

    private final java.util.Map<String, Set<WebSocketSession>> sessionsByPostId = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String postId = getPostId(session);
        if (postId == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        sessionsByPostId.computeIfAbsent(postId, k -> new CopyOnWriteArraySet<>()).add(session);
        log.info("WebSocket connected for postId: {}", postId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String postId = getPostId(session);
        if (postId != null) {
            java.util.Set<WebSocketSession> sessions = sessionsByPostId.get(postId);
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

    public void broadcastToPost(String postId, String commentId, String userId, String content) {
        Set<WebSocketSession> sessions = sessionsByPostId.get(postId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        ObjectNode json = objectMapper.createObjectNode();
        json.put("commentId", commentId);
        json.put("postId", postId);
        json.put("userId", userId);
        json.put("content", content);
        json.put("timestamp", System.currentTimeMillis());

        String message = json.toString();
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