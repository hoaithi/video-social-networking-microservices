package com.hoaithi.profile_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyStatsDTO {
    private String month; // Format: "2024-01" or "Jan 2024"
    private Integer year;
    private Integer monthNumber; // 1-12
    private Long newMembers; // Số members mới trong tháng
    private Long totalMembers; // Tổng số members tích lũy đến tháng này
    private BigDecimal revenue; // Doanh thu trong tháng
    private BigDecimal cumulativeRevenue; // Tổng doanh thu tích lũy
    private Long newPayments; // Số payments mới
}