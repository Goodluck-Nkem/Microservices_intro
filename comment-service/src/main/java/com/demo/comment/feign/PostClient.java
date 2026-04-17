package com.demo.comment.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.demo.comment.model.client.Post;
import com.demo.comment.model.client.PostWithUser;

import java.util.List;

@FeignClient(name = "post-service")
public interface PostClient {
    @GetMapping("/posts/{id}")
    public ResponseEntity<Post> getPost(@PathVariable String id);

    @GetMapping("/posts/{id}/with-user")
    public ResponseEntity<PostWithUser> getPostWithUser(@PathVariable String id);

    @GetMapping("/posts/search")
    public ResponseEntity<List<Post>> getPostsByTitle(@RequestParam String title);
}
