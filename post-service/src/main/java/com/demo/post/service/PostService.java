package com.demo.post.service;

import com.demo.post.feign.UserClient;
import com.demo.post.model.Post;
import com.demo.post.model.client.User;
import com.demo.post.repository.PostRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final UserClient userClient;

    public Post createPost(Post post) {
        User user = userClient.getUser(post.getUserId()).getBody();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");
        }
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

    public List<Post> searchByTitle(String title) {
        return postRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Post> searchByAuthor(String authorName) {
        List<User> users = userClient.getUsersByName(authorName).getBody();
        if (users == null || users.isEmpty()) {
            return List.of();
        }
        List<String> userIds = users.stream().map(User::getId).toList();
        return postRepository.findByUserIdIn(userIds);
    }

    public void deletePost(String id) {
        postRepository.deleteById(id);
    }

    public void deleteByTitle(String title) {
        List<Post> posts = postRepository.findByTitleContainingIgnoreCase(title);
        posts.stream()
            .filter(p -> p.getTitle().equalsIgnoreCase(title))
            .findFirst()
            .ifPresent(postRepository::delete);
    }
}
