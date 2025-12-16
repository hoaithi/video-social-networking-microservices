package com.hoaithi.profile_service.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipMonthlyStatsResponse {
    private List<MonthlyStatsDTO> monthlyData;
    private MembershipStatsDTO overallStats;
    private Integer totalMonths;
    private String startDate; // First payment date
    private String endDate; // Latest payment date
}