package com.demo.notification.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class PostEvent {
    private String eventType;
    private String postId;
    private String userId;
    private String title;
    private String authorName;
    private String content;
    private long timestamp;

    public PostEvent(String eventType, String postId) {
        this.eventType = eventType;
        this.postId = postId;
        this.timestamp = System.currentTimeMillis();
    }
}
