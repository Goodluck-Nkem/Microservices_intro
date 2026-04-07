package com.demo.comment.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.demo.comment.model.client.Post;
import com.demo.comment.model.client.PostWithUser;

@FeignClient(name = "post-service")
public interface PostClient {
    @GetMapping("/posts/{id}")
    public ResponseEntity<Post> getPost(@PathVariable String id);

    @GetMapping("/posts/{id}/with-user")
    public ResponseEntity<PostWithUser> getPostWithUser(@PathVariable String id);
}
