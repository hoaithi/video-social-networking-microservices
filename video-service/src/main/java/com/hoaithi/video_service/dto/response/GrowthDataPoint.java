package com.hoaithi.video_service.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrowthDataPoint {
    private String period; // "Mon", "Week 1", "Jan 2024", etc.
    private String date; // ISO date string for reference
    private Long newUsers;
    private Long activeUsers;
    private Long newVideos;
    private Long totalViews;
    private Long totalLikes;
    private Long totalComments;
    private Double engagementRate;
}