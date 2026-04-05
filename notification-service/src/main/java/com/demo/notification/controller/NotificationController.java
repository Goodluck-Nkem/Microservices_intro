package com.demo.notification.controller;

import com.demo.notification.model.CommentEvent;
import com.demo.notification.model.PostEvent;
import com.demo.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/latest/post")
    public ResponseEntity<PostEvent> getLatestPost() {
        PostEvent event = notificationService.getLatestPostEvent();
        if (event == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(event);
    }

    @GetMapping("/latest/comment")
    public ResponseEntity<CommentEvent> getLatestComment() {
        CommentEvent event = notificationService.getLatestCommentEvent();
        if (event == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(event);
    }
}
