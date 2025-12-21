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
    List<GrowthDataPoint> currentPeriod;      // Thay đổi từ growthData
    List<GrowthDataPoint> comparisonPeriod;   // Thêm mới
    GrowthSummary summary;                     // Thay đổi từ currentPeriodStats/previousPeriodStats
    String timeRange;                          // Giữ nguyên
    String comparisonType;
}