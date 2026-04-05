package com.demo.notification.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class CommentEvent {
    private String eventType;
    private long timestamp;
    private String commentId;
    private String postId;
    private String userId;
    private String commenterName;
    private String content;

    public CommentEvent(String eventType, String commentId) {
        this.eventType = eventType;
        this.commentId = commentId;
        this.timestamp = System.currentTimeMillis();
    }
}
