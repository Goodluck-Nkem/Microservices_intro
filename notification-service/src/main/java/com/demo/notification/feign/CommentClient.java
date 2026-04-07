package com.demo.notification.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.demo.notification.model.client.Comment;

@FeignClient(name = "comment-service")
public interface CommentClient {
    @GetMapping("/comments/{id}")
    public ResponseEntity<Comment> getComment(@PathVariable String id);
}

