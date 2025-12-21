package com.hoaithi.video_service.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrowthSummary {
    private Long totalNewUsers;
    private Long totalActiveUsers;
    private Long totalNewVideos;
    private Long totalViews;
    private Long totalLikes;
    private Long totalComments;

    // Comparison metrics (percentage change)
    private Double userGrowthRate;
    private Double videoGrowthRate;
    private Double viewGrowthRate;
    private Double engagementGrowthRate;

    private String startDate;
    private String endDate;
    private String comparisonStartDate;
    private String comparisonEndDate;
}