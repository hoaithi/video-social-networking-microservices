package com.hoaithi.video_service.dto.response;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class VideoResponse {
    private String id;
    private String title;
    private String description;
    private double duration; // in seconds
    private boolean isPremium;
    private String thumbnailUrl;
    private String videoUrl;
    private String userId;
}
