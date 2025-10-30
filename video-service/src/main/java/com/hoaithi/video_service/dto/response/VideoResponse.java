package com.hoaithi.video_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private Long viewCount;
    @JsonProperty("isPremium")
    private boolean isPremium;
    private String thumbnailUrl;
    private String videoUrl;
    private String profileId;
    private String profileImage;
    private String profileName;
    private LocalDateTime publishedAt;
    private Long likeCount;
    private Long dislikeCount;
    private Long commentCount;
    boolean hearted;
}
