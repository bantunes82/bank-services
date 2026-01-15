package com.bantunes82.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @KafkaListener(topics = "${kafka.ledger.topic}", groupId = "${kafka.ledger.group-id}")
    public void sendNotification(String message) {
        logger.info("Receiving the  notification: {}", message);
    }
}
