package com.demo.notification.service;

import com.demo.notification.feign.CommentClient;
import com.demo.notification.feign.PostClient;
import com.demo.notification.feign.UserClient;
import com.demo.notification.model.Comment;
import com.demo.notification.model.CommentEvent;
import com.demo.notification.model.Post;
import com.demo.notification.model.PostEvent;
import com.demo.notification.model.User;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final AtomicReference<PostEvent> latestPostEvent = new AtomicReference<>();
    private final AtomicReference<CommentEvent> latestCommentEvent = new AtomicReference<>();

    private final PostClient postClient;
    private final UserClient userClient;
    private final CommentClient commentClient;

    public void handlePostEvent(String message) {
        log.info("Received post event: {}", message);
        String[] parts = message.split(":");
        if (parts.length >= 2) {
            String eventType = parts[0];
            String postId = parts[1];
            PostEvent event = new PostEvent(eventType, postId);
            latestPostEvent.set(event);
        }
    }

    public PostEvent getLatestPostEvent() {
        PostEvent event = latestPostEvent.get();
        if (event != null) {
            try {

                Post post = postClient.getPost(event.getPostId()).getBody();
                if(post == null)
                    throw new RuntimeException("postClient.getPost() returned a null body!");
                event.setUserId(post.getUserId());
                event.setTitle(post.getTitle());
                event.setContent(post.getContent());

                User user = userClient.getUser(event.getUserId()).getBody();
                if(user == null)
                    throw new RuntimeException("userClient.getUser() returned a null body!");
                event.setAuthorName(user.getName());

            } catch (Exception e) {
                log.warn("Failed to fetch complete post details for: {}", event.getPostId());
            }
        }
        return event;
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

    public CommentEvent getLatestCommentEvent() {
        CommentEvent event = latestCommentEvent.get();
        if(event != null){
            try {
                Comment comment = commentClient.getComment(event.getCommentId()).getBody();
                if(comment == null)
                    throw new RuntimeException("commentClient.getComment() returned a null body!");
                event.setPostId(comment.getPostId());
                event.setUserId(comment.getUserId());
                event.setContent(comment.getContent());

                User user = userClient.getUser(event.getUserId()).getBody();
                if(user == null)
                    throw new RuntimeException("userClient.getUser() returned a null body!");
                event.setCommenterName(user.getName());
            } catch (Exception e) {
                log.warn("Failed to fetch complete comment details for: {}", event.getCommentId());
            }
        }
        return event;
    }
}
