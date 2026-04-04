package com.demo.comment.controller;

import com.demo.comment.feign.PostClient;
import com.demo.comment.feign.UserClient;
import com.demo.comment.model.Comment;
import com.demo.comment.model.Post;
import com.demo.comment.model.PostWithUser;
import com.demo.comment.model.User;
import com.demo.comment.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import lombok.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/comments")
public class CommentController {
    private final CommentService commentService;
    private final UserClient userClient;
    private final PostClient postClient;

    @PostMapping
    public ResponseEntity<Comment> createComment(@RequestBody Comment comment) {
        return ResponseEntity.ok(commentService.createComment(comment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comment> getComment(@PathVariable String id) {
        return ResponseEntity.ok(commentService.getCommentById(id));
    }

    @GetMapping
    public ResponseEntity<List<Comment>> getAllComments() {
        return ResponseEntity.ok(commentService.getAllComments());
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<Comment>> getCommentsByPost(@PathVariable String postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }

    @GetMapping("/test-circuit-breaker/user/{userId}")
    public ResponseEntity<?> testUserCircuitBreaker(@PathVariable String userId) {
        return ResponseEntity.ok(commentService.getUser(userId));
    }

    @GetMapping("/test-circuit-breaker/post/{postId}")
    public ResponseEntity<?> testPostCircuitBreaker(@PathVariable String postId) {
        return ResponseEntity.ok(commentService.getPost(postId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable String id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/detailed")
    public ResponseEntity<CommentDetailed> getCommentDetailed(@PathVariable String id) {
        Comment comment = commentService.getCommentById(id);
        try {
            User user = userClient.getUser(comment.getUserId()).getBody();
            PostWithUser postWithUser = postClient.getPostWithUser(comment.getPostId()).getBody();
            return ResponseEntity.ok(new CommentDetailed(comment, user, postWithUser));
        } catch (Exception e) {
            return ResponseEntity.ok(new CommentDetailed(comment, null, null));
        }
    }

    record CommentDetailed(Comment comment, User user, PostWithUser postWithUser) {}
}
