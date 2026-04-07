package com.demo.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document(collection = "users")
public class User {
    @Id
    @EqualsAndHashCode.Include
    private String id;

    @NonNull
    private String name;
    
    @NonNull
    private String email;
}
