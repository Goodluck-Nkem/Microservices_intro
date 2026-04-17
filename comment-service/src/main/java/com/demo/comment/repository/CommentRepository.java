package com.demo.comment.repository;

import com.demo.comment.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByPostId(String postId);
    List<Comment> findByPostIdIn(List<String> postIds);
    List<Comment> findByUserIdIn(List<String> userIds);
}
