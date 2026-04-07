package com.demo.notification.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.demo.notification.model.client.Post;

@FeignClient(name = "post-service")
public interface PostClient {
    @GetMapping("/posts/{id}")
    public ResponseEntity<Post> getPost(@PathVariable String id);
}
