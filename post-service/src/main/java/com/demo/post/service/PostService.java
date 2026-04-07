package com.demo.post.service;

import com.demo.post.model.Post;
import com.demo.post.repository.PostRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public Post createPost(Post post) {
        Post saved = postRepository.save(post);
        kafkaTemplate.send("post-events", "POST_CREATED:" + saved.getId());
        return saved;
    }

    public Post getPostById(String id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found: " + id));
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public List<Post> getPostsByUserId(String userId) {
        return postRepository.findByUserId(userId);
    }

    public void deletePost(String id) {
        postRepository.deleteById(id);
    }
}
