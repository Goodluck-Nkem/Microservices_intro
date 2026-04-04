package com.demo.comment.model;

import lombok.*;

@NoArgsConstructor
@Setter
@Getter
public class Post {
    private String id;
    private String userId;
    private String title;
    private String content;
}
