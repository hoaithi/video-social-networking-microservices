package com.hoaithi.video_service.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewStatsResponse {
    private Long totalViews;
    private Long validViews;
    private Long uniqueViewers;
    private Double averageWatchPercentage;
    private Double totalWatchDuration;
    private Double validViewRate;
}