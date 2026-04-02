package com.demo.notification.model;

public class CommentEvent {
    private String eventType;
    private String commentId;
    private String postId;
    private String userId;
    private String content;
    private long timestamp;

    public CommentEvent() {}

    public CommentEvent(String eventType, String commentId) {
        this.eventType = eventType;
        this.commentId = commentId;
        this.timestamp = System.currentTimeMillis();
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
