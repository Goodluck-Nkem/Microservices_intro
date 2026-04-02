package com.demo.notification.kafka;

import com.demo.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    private final NotificationService notificationService;

    public KafkaConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

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
