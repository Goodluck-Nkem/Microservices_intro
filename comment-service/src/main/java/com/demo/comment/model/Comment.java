package com.demo.comment.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

@RequiredArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document(collection = "comments")
public class Comment {
    @Id
    @EqualsAndHashCode.Include
    private String id;

    @NonNull
    private String postId;

    @NonNull
    private String userId;

    @NonNull
    private String content;
}
