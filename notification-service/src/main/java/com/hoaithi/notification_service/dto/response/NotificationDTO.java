package com.hoaithi.notification_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hoaithi.notification_service.enums.EntityType;
import com.hoaithi.notification_service.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private String id;
    private NotificationType type;
    private LocalDateTime createdAt;
    @JsonProperty("isRead")
    private boolean isRead = false;
    private String content;
    private String entityId;
    private EntityType entityType;
    private String userId;
    private String actorId;
    private String fullName;
    private String avatarUrl;
}
