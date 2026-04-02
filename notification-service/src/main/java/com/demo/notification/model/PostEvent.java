package com.demo.notification.model;

public class PostEvent {
    private String eventType;
    private String postId;
    private String userId;
    private String title;
    private long timestamp;

    public PostEvent() {}

    public PostEvent(String eventType, String postId) {
        this.eventType = eventType;
        this.postId = postId;
        this.timestamp = System.currentTimeMillis();
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
