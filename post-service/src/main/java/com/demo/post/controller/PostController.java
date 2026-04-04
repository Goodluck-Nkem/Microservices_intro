package com.demo.post.controller;

import com.demo.post.feign.UserClient;
import com.demo.post.model.Post;
import com.demo.post.model.User;
import com.demo.post.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post post) {
        return ResponseEntity.ok(postService.createPost(post));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPost(@PathVariable String id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Post>> getPostsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(postService.getPostsByUserId(userId));
    }

    @GetMapping("/{id}/with-user")
    public ResponseEntity<PostWithUser> getPostWithUser(@PathVariable String id) {
        Post post = postService.getPostById(id);
        try {
            User user = userClient.getUser(post.getUserId()).getBody();
            log.info("{id}/with-user -> getUser() successful", id);
            return ResponseEntity.ok(new PostWithUser(post, user));
        } catch (Exception e) {
            log.info("{id}/with-user -> getUser() failed", id);
            return ResponseEntity.ok(new PostWithUser(post, null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable String id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    record PostWithUser(Post post, User user) {}
}
