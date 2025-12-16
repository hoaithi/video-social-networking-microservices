package com.hoaithi.video_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyVideoStatsDTO {
    private String month; // Format: "Jan 2024"
    private Integer year;
    private Integer monthNumber; // 1-12

    // Video stats
    private Long newVideos; // Số videos upload trong tháng
    private Long totalVideos; // Tổng số videos tích lũy

    // Interaction stats
    private Long newViews; // Views mới trong tháng
    private Long totalViews; // Tổng views tích lũy
    private Long newLikes; // Likes mới trong tháng
    private Long totalLikes; // Tổng likes tích lũy
    private Long newComments; // Comments mới trong tháng
    private Long totalComments; // Tổng comments tích lũy

    // Engagement metrics
    private Double averageEngagementRate; // (likes + comments) / views * 100
    private Long newSubscribers; // Subscribers mới trong tháng (from profile service)
}