package com.demo.notification.kafka;

import com.demo.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "post-events", groupId = "notification-group")
    public void consumePostEvent(String message) {
        log.info("Consumed from post-events: {}", message);
        notificationService.handlePostEvent(message);
    }

    @KafkaListener(topics = "comment-events", groupId = "notification-group")
    public void consumeCommentEvent(String message) {
        log.info("Consumed from comment-events: {}", message);
        notificationService.handleCommentEvent(message);
    }
}
