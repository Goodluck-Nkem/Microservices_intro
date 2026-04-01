package com.demo.comment.service;

import com.demo.comment.model.Comment;
import com.demo.comment.model.Post;
import com.demo.comment.model.User;
import com.demo.comment.repository.CommentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public CommentService(CommentRepository commentRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.commentRepository = commentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Comment createComment(Comment comment) {
        Comment saved = commentRepository.save(comment);
        kafkaTemplate.send("comment-events", "COMMENT_CREATED:" + saved.getId());
        return saved;
    }

    public Comment getCommentById(String id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found: " + id));
    }

    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    public List<Comment> getCommentsByPostId(String postId) {
        return commentRepository.findByPostId(postId);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    public User getUser(String userId) {
        throw new RuntimeException("Simulated user service failure");
    }

    public User getUserFallback(String userId, Throwable t) {
        User fallback = new User();
        fallback.setId(userId);
        fallback.setName("Unknown (Service Unavailable)");
        fallback.setEmail("unavailable@fallback");
        return fallback;
    }

    @CircuitBreaker(name = "postService", fallbackMethod = "getPostFallback")
    public Post getPost(String postId) {
        throw new RuntimeException("Simulated post service failure");
    }

    public Post getPostFallback(String postId, Throwable t) {
        Post fallback = new Post();
        fallback.setId(postId);
        fallback.setTitle("Unknown (Service Unavailable)");
        return fallback;
    }

    public void deleteComment(String id) {
        commentRepository.deleteById(id);
        kafkaTemplate.send("comment-events", "COMMENT_DELETED:" + id);
    }
}
