package com.hoaithi.notification_service.service;

import com.hoaithi.event.dto.VideoUploadedEvent;
import com.hoaithi.notification_service.dto.response.NotificationDTO;
import com.hoaithi.notification_service.entity.Notification;
import com.hoaithi.notification_service.entity.Subscription;
import com.hoaithi.notification_service.enums.EntityType;
import com.hoaithi.notification_service.enums.NotificationType;
import com.hoaithi.notification_service.exception.AppException;
import com.hoaithi.notification_service.exception.ErrorCode;
import com.hoaithi.notification_service.repository.NotificationRepository;
import com.hoaithi.notification_service.repository.SubscriptionRepository;
import com.hoaithi.notification_service.utils.ProfileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ProfileUtil profileUtil;

    public void handleVideoUploaded(VideoUploadedEvent event) {

        // Lấy danh sách tất cả subscriber của uploader
        List<Subscription> subscribers =
                subscriptionRepository.findAllByChannelId(event.getChannelId());

        for (Subscription s : subscribers) {
            Notification notification = Notification.builder()
                    .userId(s.getSubscriberId())               // người nhận
                    .actorId(event.getChannelId())            // uploader
                    .entityId(event.getVideoId())              // video id
                    .entityType(EntityType.VIDEO)
                    .type(NotificationType.NEW_VIDEO)
                    .content(event.getTitle())                 // nội dung thông báo
                    .isRead(false)
                    .avatarUrl(event.getAvatarUrl())
                    .fullName(event.getFullName())
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
        }
    }
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return notificationRepository.countByUserIdAndIsRead(profileUtil.getCurrentUserId(), false);
    }

    public Page<NotificationDTO> getUserNotifications(Pageable pageable) {
        String userId = profileUtil.getCurrentUserId();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToNotificationDTO);
    }

    @Transactional
    public void markAsRead(String notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // Kiểm tra xem thông báo có thuộc về người dùng không
        if (!notification.getUserId().equals(profileUtil.getCurrentUserId())) {
            throw new AppException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead() {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(profileUtil.getCurrentUserId(), false);

        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }

        notificationRepository.saveAll(unreadNotifications);
    }

    private NotificationDTO mapToNotificationDTO(Notification notification) {

        return NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .content(notification.getContent())
                .createdAt(notification.getCreatedAt())
                .isRead(notification.isRead())
                .entityId(notification.getEntityId())
                .entityType(notification.getEntityType())
                .actorId(notification.getActorId())
                .avatarUrl(notification.getAvatarUrl())
                .fullName(notification.getFullName())
                .build();
    }
}
