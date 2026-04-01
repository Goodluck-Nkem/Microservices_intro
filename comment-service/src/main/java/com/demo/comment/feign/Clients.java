package com.demo.comment.feign;

import com.demo.comment.model.Post;
import com.demo.comment.model.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/users/{id}")
    User getUser(@PathVariable("id") String id);
}

@FeignClient(name = "post-service")
public interface PostClient {
    @GetMapping("/posts/{id}")
    Post getPost(@PathVariable("id") String id);
}
