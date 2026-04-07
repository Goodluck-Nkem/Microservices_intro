package com.demo.post.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@NoArgsConstructor
@RequiredArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document(collection = "posts")
public class Post {
    @Id
    @EqualsAndHashCode.Include
    private String id;

    @NonNull
    private String userId;

    @NonNull
    private String title;

    @NonNull
    private String content;
}
