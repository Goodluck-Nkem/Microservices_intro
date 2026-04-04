package com.demo.comment.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

@RequiredArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Document(collection = "comments")
public class Comment {
    @Id
    private String id;
    @NonNull
    private String postId;
    @NonNull
    private String userId;
    @NonNull
    private String content;
}
