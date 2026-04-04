package com.demo.post.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@NoArgsConstructor
@RequiredArgsConstructor
@Setter
@Getter
@Document(collection = "posts")
public class Post {
    @Id
    private String id;
    @NonNull
    private String userId;
    @NonNull
    private String title;
    @NonNull
    private String content;
}
