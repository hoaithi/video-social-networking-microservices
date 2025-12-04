package com.hoaithi.video_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopVideo {
    private String id;
    private String title;
    private String description;
    private Long duration;
    private Long viewCount;
    private String thumbnailUrl;
    private String videoUrl;
    private String profileId;
    private String profileImage;
    private String profileName;
    private String publishedAt;
    private Long likeCount;
    private Long dislikeCount;
    private Long commentCount;
    private boolean hearted;
    private Double engagementScore;
    private boolean isPremium;
}