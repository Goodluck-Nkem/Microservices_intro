package com.demo.post.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.demo.post.model.client.User;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable String id);
}
