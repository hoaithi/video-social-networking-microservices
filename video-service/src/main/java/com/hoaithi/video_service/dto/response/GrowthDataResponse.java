package com.hoaithi.video_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GrowthDataResponse {
    List<VideoGrowthStatsDTO> growthData;
    Stats currentPeriodStats;
    Stats previousPeriodStats;
    String timeRange; // "week", "month", "year"
    String comparisonType; // "previous", "lastYear", "none"
    String startDate;
    String endDate;
}