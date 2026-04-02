package com.demo.notification.service;

import com.demo.notification.feign.PostClient;
import com.demo.notification.feign.UserClient;
import com.demo.notification.model.CommentEvent;
import com.demo.notification.model.Post;
import com.demo.notification.model.PostEvent;
import com.demo.notification.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final AtomicReference<PostEvent> latestPostEvent = new AtomicReference<>();
    private final AtomicReference<CommentEvent> latestCommentEvent = new AtomicReference<>();
    private final PostClient postClient;
    private final UserClient userClient;

    public NotificationService(PostClient postClient, UserClient userClient) {
        this.postClient = postClient;
        this.userClient = userClient;
    }

    public void handlePostEvent(String message) {
        log.info("Received post event: {}", message);
        String[] parts = message.split(":");
        if (parts.length >= 2) {
            String eventType = parts[0];
            String postId = parts[1];
            PostEvent event = new PostEvent(eventType, postId);
            latestPostEvent.set(event);

            try {
                Post post = postClient.getPost(postId);
                event.setTitle(post.getTitle());
                event.setUserId(post.getUserId());
            } catch (Exception e) {
                log.warn("Failed to fetch post details for: {}", postId);
            }
        }
    }

    public void handleCommentEvent(String message) {
        log.info("Received comment event: {}", message);
        String[] parts = message.split(":");
        if (parts.length >= 2) {
            String eventType = parts[0];
            String commentId = parts[1];
            CommentEvent event = new CommentEvent(eventType, commentId);
            latestCommentEvent.set(event);
        }
    }

    public PostEvent getLatestPostEvent() {
        PostEvent event = latestPostEvent.get();
        if (event != null && event.getUserId() != null) {
            try {
                User user = userClient.getUser(event.getUserId());
                event.setUserId(user.getName());
            } catch (Exception e) {
                log.warn("Failed to fetch user details");
            }
        }
        return event;
    }

    public CommentEvent getLatestCommentEvent() {
        return latestCommentEvent.get();
    }
}
