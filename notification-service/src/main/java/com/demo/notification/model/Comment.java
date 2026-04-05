package com.demo.notification.model;

import lombok.*;

@NoArgsConstructor
@Setter
@Getter
public class Comment {
    private String id;
    private String postId;
    private String userId;
    private String content;
}
