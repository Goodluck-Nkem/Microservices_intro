package com.demo.comment.feign;

import com.demo.comment.model.Post;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "post-service")
public interface PostClient {
    @GetMapping("/posts/{id}")
    Post getPost(@PathVariable("id") String id);
}
