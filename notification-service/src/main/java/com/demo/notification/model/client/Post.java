package com.demo.notification.model.client;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class Post {
    private String id;
    private String userId;
    private String title;
    private String content;
}
