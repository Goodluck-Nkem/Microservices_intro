package com.demo.comment.controller;

import com.demo.comment.model.Comment;
import com.demo.comment.model.client.PostWithUser;
import com.demo.comment.model.client.User;
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable String id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/detailed")
    public ResponseEntity<CommentDetailed> getCommentDetailed(@PathVariable String id) {
        Comment comment = commentService.getCommentById(id);
        User user = commentService.getUser(comment.getUserId());
        PostWithUser postWithAuthor = commentService.getPostAndAuthor(comment.getPostId());
        return ResponseEntity.ok(new CommentDetailed(comment, user, postWithAuthor));
    }

    @GetMapping("/test-circuit-breaker/user/{userId}")
    public ResponseEntity<?> testUserCircuitBreakerWithFakeId(@PathVariable String userId) {
        return ResponseEntity.ok(commentService.getUser(userId));
    }

    @GetMapping("/test-circuit-breaker/post/{postId}")
    public ResponseEntity<?> testPostCircuitBreakerWithFakeId(@PathVariable String postId) {
        return ResponseEntity.ok(commentService.getPost(postId));
    }

    record CommentDetailed(Comment comment, User user, PostWithUser postWithAuthor) {}

}
