//package com.hoaithi.notification_service.controller;
//
//import com.hoaithi.event.dto.CreationUserEvent;
//import com.hoaithi.event.dto.ForgetPasswordEvent;
//import com.hoaithi.notification_service.service.EmailService;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class NotificationController {
//
//
//    EmailService emailService;
//    /**
//     * This method listens to the "user-topic" Kafka topic and processes incoming messages.
//     * It logs the received message for notification purposes.
//     *
//     * @param event The message received from the Kafka topic.
//     */
//    @KafkaListener(topics = "user-topic", groupId = "notification-group")
//    public void createUserNotification(CreationUserEvent event) {
//        emailService.sendEmail(event.getEmail(),
//                "Welcome to our service, Video Social Networking Platform"
//                , "Thank you for registering with Video Social Networking Platform!");
//    }
//
//    @KafkaListener(topics = "forget-password", groupId = "notification-group")
//    public void sendOtpForgetPassword(ForgetPasswordEvent event){
//        emailService.sendEmail(event.getEmail(), "send otp reset password", event.getOtp());
//    }
//}


package com.hoaithi.notification_service.consumer;

import com.hoaithi.event.dto.*;
import com.hoaithi.notification_service.service.EmailService;
import com.hoaithi.notification_service.service.NotificationService;
import com.hoaithi.notification_service.service.SubscriptionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaConsumer {

    EmailService emailService;
    SubscriptionService subscriptionService;
    NotificationService notificationService;

    /**
     * Lắng nghe topic user-topic và gửi email chào mừng
     */
    @KafkaListener(topics = "user-topic", groupId = "notification-group")
    public void createUserNotification(CreationUserEvent event) {
        log.info("Received user creation event for email: {}", event.getEmail());

        // Trích xuất username từ email (hoặc từ event nếu có field username)
        String username = extractUsernameFromEmail(event.getEmail());

        emailService.sendWelcomeEmail(event.getEmail(), username);

        log.info("Welcome email sent successfully to: {}", event.getEmail());
    }

    /**
     * Lắng nghe topic forget-password và gửi OTP
     */
    @KafkaListener(topics = "forget-password", groupId = "notification-group")
    public void sendOtpForgetPassword(ForgetPasswordEvent event) {
        log.info("Received forget password event for email: {}", event.getEmail());

        emailService.sendOtpEmail(event.getEmail(), event.getOtp());

        log.info("OTP email sent successfully to: {}", event.getEmail());
    }

    /**
     * Helper method để trích xuất username từ email
     * Ví dụ: user@example.com -> user
     */
    private String extractUsernameFromEmail(String email) {
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        return "User";
    }
    @KafkaListener(topics = "subscribe-topic", groupId = "notification-group")
    public void subscribe(UserSubscribedEvent event) {
        log.info("[SUBSCRIBE_EVENT] subscriberId={}, channelId={}",
                event.getSubscriberId(), event.getChannelId());
        subscriptionService.handleSubscribed(event);
    }

    @KafkaListener(topics = "unsubscribe-topic", groupId = "notification-group")
    public void unsubscribe(UserUnsubscribedEvent event) {
        log.info("[UNSUBSCRIBE_EVENT] subscriberId={}, channelId={}",
                event.getUnsubscriberId(), event.getChannelId());
        subscriptionService.handleUnsubscribed(event);
    }

    @KafkaListener(topics = "video-upload", groupId = "notification-service")
    public void listenVideoUploaded(VideoUploadedEvent event) {
        notificationService.handleVideoUploaded(event);
    }


}
