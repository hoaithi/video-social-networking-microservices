package com.hoaithi.notification_service.controller;

import com.hoaithi.notification_service.dto.response.ApiResponse;
import com.hoaithi.notification_service.dto.response.NotificationDTO;
import com.hoaithi.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NotificationDTO> notifications = notificationService.getUserNotifications(pageable);

        return ResponseEntity.ok(ApiResponse.<Page<NotificationDTO>>builder()
                .message("Notifications retrieved successfully")
                .result(notifications)
                .build());
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        long count = notificationService.getUnreadCount();

        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .message("Unread notification count retrieved successfully")
                .result(count)
                .build());
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable String id) {

        notificationService.markAsRead(id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Notification marked as read")
                .build());
    }
    @PutMapping("/read-all")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("All notifications marked as read")
                .build());
    }
}
