package com.hoaithi.notification_service.controller;

import com.hoaithi.notification_service.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    EmailService emailService;
    /**
     * This method listens to the "user-topic" Kafka topic and processes incoming messages.
     * It logs the received message for notification purposes.
     *
     * @param email The message received from the Kafka topic.
     */
    @KafkaListener(topics = "user-topic", groupId = "notification-group")
    public void createUserNotification(String email) {
        emailService.sendEmail(email,
                "Welcome to our service, Video Social Networking Platform"
                , "Thank you for registering with Video Social Networking Platform!");
    }
}
