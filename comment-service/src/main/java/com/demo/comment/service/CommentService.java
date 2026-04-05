package com.demo.comment.service;

import com.demo.comment.feign.PostClient;
import com.demo.comment.feign.UserClient;
import com.demo.comment.model.Comment;
import com.demo.comment.model.Post;
import com.demo.comment.model.PostWithUser;
import com.demo.comment.model.User;
import com.demo.comment.repository.CommentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import lombok.*;

@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final UserClient userClient;
    private final PostClient postClient;


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
        return userClient.getUser(userId).getBody();
    }

    public User getUserFallback(String userId, Throwable t) {
        User fallback = new User();
        fallback.setId(userId);
        fallback.setName("Unknown (Service Unavailable)");
        fallback.setEmail(null);
        return fallback;
    }

    @CircuitBreaker(name = "postService", fallbackMethod = "getPostFallback")
    public Post getPost(String postId) {
        Post post = postClient.getPost(postId).getBody();
        return post;
    }

    public Post getPostFallback(String postId, Throwable t) {
        Post fallback = new Post();
        fallback.setId(postId);
        fallback.setTitle("Unknown (Service Unavailable)");
        return fallback;
    }

    @CircuitBreaker(name = "postService", fallbackMethod = "getPostAndAuthorFallback")
    public PostWithUser getPostAndAuthor(String postId) {
        return postClient.getPostWithUser(postId).getBody();
    }

    public PostWithUser getPostAndAuthorFallback(String postId, Throwable t) {
        PostWithUser fallback = new PostWithUser(
            getPostFallback(postId, t),
            getUserFallback(null, t)
        );
        return fallback;
    }

    public void deleteComment(String id) {
        commentRepository.deleteById(id);
        kafkaTemplate.send("comment-events", "COMMENT_DELETED:" + id);
    }
}
