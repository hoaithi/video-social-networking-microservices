package com.hoaithi.profile_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipStatsDTO {
    private Long totalMemberships;
    private Long activeMemberships;
    private Long totalMembershipTiers;
    private Long activeMembershipTiers;
    private Long totalPayments;
    private Long completedPayments;
    private BigDecimal totalRevenue;

    // For channel creators
    private Long totalMembers; // Total users who subscribed to this channel
    private BigDecimal channelRevenue; // Total revenue from membership sales
}
