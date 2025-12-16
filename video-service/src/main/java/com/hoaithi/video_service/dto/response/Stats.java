package com.hoaithi.video_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Stats {
    private Long totalViews;
    private Long totalLikes;
    private Long totalDislikes;
    private Long totalComments;
    private Long totalVideos;

    Long totalUsers;
    Long activeUsers;
}
