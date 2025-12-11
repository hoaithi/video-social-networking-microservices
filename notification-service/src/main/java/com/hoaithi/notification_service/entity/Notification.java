package com.hoaithi.notification_service.entity;
import com.hoaithi.notification_service.enums.EntityType;
import com.hoaithi.notification_service.enums.NotificationType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    private String id;

    private String userId;        // Người nhận thông báo
    private String actorId;       // Người tạo ra hành động (uploader, commenter,...)
    private String fullName;
    private String avatarUrl;

    private NotificationType type;        // VIDEO_UPLOAD, COMMENT, POST, ...
    private String content;     // Nội dung hiển thị
    private boolean isRead = false;

    private String entityId;      // videoId, commentId, ...
    private EntityType entityType;  // VIDEO, COMMENT, POST,...

    private LocalDateTime createdAt;
}
