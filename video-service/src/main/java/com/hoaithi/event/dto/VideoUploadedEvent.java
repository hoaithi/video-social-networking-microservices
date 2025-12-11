package com.hoaithi.event.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoUploadedEvent {

    private String videoId;       // ID video
    private String channelId;    // User đã upload
    private String title;       // Tiêu đề video
    private String thumbnailUrl; // Thumbnail (optional)
    private String description; // Mô tả ngắn (optional)
    private String fullName;
    private String avatarUrl;

    private LocalDateTime uploadedAt; // Thời gian upload (UTC)

}