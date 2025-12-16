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
public class VideoMonthlyStatsResponse {
    private List<MonthlyVideoStatsDTO> monthlyData;
    private Stats overallStats; // Reuse existing Stats class
    private Long totalSubscribers;
    private Integer totalMonths;
    private String startDate; // First video date
    private String endDate; // Latest video date
}
