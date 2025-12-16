package com.hoaithi.profile_service.service;


import com.hoaithi.profile_service.dto.request.MembershipTierCreateRequest;
import com.hoaithi.profile_service.dto.request.MembershipTierUpdateRequest;
import com.hoaithi.profile_service.dto.response.MembershipMonthlyStatsResponse;
import com.hoaithi.profile_service.dto.response.MembershipStatsDTO;
import com.hoaithi.profile_service.dto.response.MembershipTierDTO;
import com.hoaithi.profile_service.dto.response.MonthlyStatsDTO;
import com.hoaithi.profile_service.entity.MembershipTier;
import com.hoaithi.profile_service.entity.Profile;
import com.hoaithi.profile_service.enums.PaymentStatus;
import com.hoaithi.profile_service.exception.AppException;
import com.hoaithi.profile_service.exception.ErrorCode;
import com.hoaithi.profile_service.repository.MembershipRepository;
import com.hoaithi.profile_service.repository.MembershipTierRepository;
import com.hoaithi.profile_service.repository.PaymentRepository;
import com.hoaithi.profile_service.repository.ProfileRepository;
import com.hoaithi.profile_service.utils.ProfileUtil;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@Slf4j
public class MembershipService {

    MembershipTierRepository membershipTierRepository;
    MembershipRepository membershipRepository;
    ProfileRepository profileRepository;
    ProfileUtil profileUtil;
    PaymentRepository paymentRepository;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");

    @Transactional
    public MembershipTierDTO createMembershipTier(MembershipTierCreateRequest request) {

        Profile user = profileRepository.findById(profileUtil.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_EXISTED));

        // Tạo membership tier mới
        MembershipTier membershipTier = MembershipTier.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .durationMonths(request.getDurationMonths())
                .isActive(true)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        MembershipTier savedTier = membershipTierRepository.save(membershipTier);

        return mapToMembershipTierDTO(savedTier);
    }

    @Transactional
    public MembershipTierDTO updateMembershipTier(Long tierId, MembershipTierUpdateRequest request) {
        MembershipTier tier = membershipTierRepository.findById(tierId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_EXISTED));
        String userId = profileUtil.getCurrentUserId();

        // Kiểm tra quyền sở hữu
        if (!tier.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        // Cập nhật thông tin
        if (request.getName() != null) {
            tier.setName(request.getName());
        }

        if (request.getDescription() != null) {
            tier.setDescription(request.getDescription());
        }

        if (request.getPrice() != null) {
            tier.setPrice(request.getPrice());
        }

        if (request.getDurationMonths() != null) {
            tier.setDurationMonths(request.getDurationMonths());
        }

        if (request.getIsActive() != null) {
            tier.setActive(request.getIsActive());
        }

        tier.setUpdatedAt(LocalDateTime.now());

        MembershipTier updatedTier = membershipTierRepository.save(tier);

        return mapToMembershipTierDTO(updatedTier);
    }

    @Transactional
    public void deleteMembershipTier(Long tierId) {
        MembershipTier tier = membershipTierRepository.findById(tierId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_EXISTED));

        String userId = profileUtil.getCurrentUserId();

        // Kiểm tra quyền sở hữu
        if (!tier.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        // Kiểm tra xem có subscription active nào đang sử dụng tier này không
        long activeSubscriptions = membershipRepository.countByMembershipTierIdAndIsActive(tierId, true);
        if (activeSubscriptions > 0) {
            // Thay vì xóa, ta đánh dấu là không active
            tier.setActive(false);
            tier.setUpdatedAt(LocalDateTime.now());
            membershipTierRepository.save(tier);
        } else {
            membershipTierRepository.delete(tier);
        }
    }

    @Transactional(readOnly = true)
    public List<MembershipTierDTO> getChannelMembershipTiers(String channelId) {
        Profile user = profileRepository.findById(profileUtil.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_EXISTED));

        return membershipTierRepository.findByUserIdOrderByPriceAsc(channelId).stream()
                .map(this::mapToMembershipTierDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MembershipTierDTO getMembershipTierById(Long tierId) {
        MembershipTier tier = membershipTierRepository.findById(tierId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_EXISTED));

        return mapToMembershipTierDTO(tier);
    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<MembershipDTO> getUserMemberships(Long userId) {
//        return membershipRepository.findByUserId(userId).stream()
//                .map(this::mapToMembershipDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<MembershipDTO> getChannelMembers(Long channelId) {
//        return membershipRepository.findByChannelId(channelId).stream()
//                .map(this::mapToMembershipDTO)
//                .collect(Collectors.toList());
//    }

    @Transactional(readOnly = true)
    public boolean hasActiveChannelMembership(String channelId) {
        String userId = profileUtil.getCurrentUserId();
        return membershipRepository.existsByUserIdAndChannelIdAndIsActive(userId, channelId, true);
    }

    // Helper methods to map entities to DTOs
    private MembershipTierDTO mapToMembershipTierDTO(MembershipTier tier) {
        return MembershipTierDTO.builder()
                .id(tier.getId())
                .name(tier.getName())
                .description(tier.getDescription())
                .price(tier.getPrice())
                .durationMonths(tier.getDurationMonths())
                .isActive(tier.isActive())
                .createdAt(tier.getCreatedAt())
                .updatedAt(tier.getUpdatedAt())
                .channelId(tier.getUser().getId())
                .channelName(tier.getUser().getFullName())
                .build();
    }

//    private MembershipDTO mapToMembershipDTO(Membership membership) {
//        MembershipTier tier = membership.getMembershipTier();
//        User subscriber = membership.getUser();
//        User channel = tier.getUser();
//
//        return MembershipDTO.builder()
//                .id(membership.getId())
//                .startDate(membership.getStartDate())
//                .endDate(membership.getEndDate())
//                .isActive(membership.isActive())
//                .createdAt(membership.getCreatedAt())
//                .updatedAt(membership.getUpdatedAt())
//                .tier(mapToMembershipTierDTO(tier))
//                .subscriber(UserDTO.builder()
//                        .id(subscriber.getId())
//                        .username(subscriber.getUsername())
//                        .profilePicture(subscriber.getProfilePicture())
//                        .build())
//                .channel(UserDTO.builder()
//                        .id(channel.getId())
//                        .username(channel.getUsername())
//                        .channelName(channel.getChannelName())
//                        .channelPicture(channel.getChannelPicture())
//                        .build())
//                .build();
//    }


    /**
     * Get membership statistics for a user profile
     * This includes memberships the user has purchased
     */
    public MembershipStatsDTO getUserMembershipStats(String profileId) {
        log.info("=== Getting Membership Stats for Profile: {} ===", profileId);

        Long totalMemberships = membershipRepository.countByProfileId(profileId);
        Long activeMemberships = membershipRepository.countActiveByProfileId(profileId);
        Long totalPayments = paymentRepository.countByProfileId(profileId);
        Long completedPayments = paymentRepository.countByProfileIdAndStatus(
                profileId, PaymentStatus.COMPLETED);
        BigDecimal totalSpent = paymentRepository.sumAmountByProfileId(profileId);

        log.info("User stats - Memberships: {}, Active: {}, Payments: {}, Completed: {}, Total Spent: {}",
                totalMemberships, activeMemberships, totalPayments, completedPayments, totalSpent);

        return MembershipStatsDTO.builder()
                .activeMemberships(activeMemberships)
                .totalPayments(totalPayments)
                .completedPayments(completedPayments)
                .totalRevenue(totalSpent)
                .build();
    }

    /**
     * Get channel statistics for a content creator
     * This includes membership tiers created and members who subscribed
     */
    public MembershipStatsDTO getChannelMembershipStats(String profileId) {
        log.info("=== Getting Channel Membership Stats for Profile: {} ===", profileId);

        Long totalTiers = membershipTierRepository.countByProfileId(profileId);
        Long activeTiers = membershipTierRepository.countActiveByProfileId(profileId);
        Long totalMembers = membershipRepository.countMembersByChannelId(profileId);
        BigDecimal channelRevenue = paymentRepository.sumRevenueByChannelId(profileId);

        log.info("Channel stats - Tiers: {}, Active Tiers: {}, Members: {}, Revenue: {}",
                totalTiers, activeTiers, totalMembers, channelRevenue);

        return MembershipStatsDTO.builder()
                .totalMembershipTiers(totalTiers)
                .activeMembershipTiers(activeTiers)
                .totalMembers(totalMembers)
                .channelRevenue(channelRevenue)
                .build();
    }

    /**
     * Get comprehensive membership statistics
     * Includes both user and channel perspectives
     */
    public MembershipStatsDTO getComprehensiveMembershipStats(String profileId) {
        log.info("=== Getting Comprehensive Membership Stats for Profile: {} ===", profileId);

        MembershipStatsDTO userStats = getUserMembershipStats(profileId);
        MembershipStatsDTO channelStats = getChannelMembershipStats(profileId);

        return MembershipStatsDTO.builder()
                // User perspective (memberships purchased)
                .totalMemberships(userStats.getTotalMemberships())
                .activeMemberships(userStats.getActiveMemberships())
                .totalPayments(userStats.getTotalPayments())
                .completedPayments(userStats.getCompletedPayments())
                .totalRevenue(userStats.getTotalRevenue())
                // Channel perspective (content creator)
                .totalMembershipTiers(channelStats.getTotalMembershipTiers())
                .activeMembershipTiers(channelStats.getActiveMembershipTiers())
                .totalMembers(channelStats.getTotalMembers())
                .channelRevenue(channelStats.getChannelRevenue())
                .build();
    }


    /**
     * Get monthly statistics for channel (revenue and member growth over time)
     * @param profileId Channel's profile ID
     * @param months Number of months to look back (default: 6)
     * @return Monthly statistics with revenue and member data
     */
    public MembershipMonthlyStatsResponse getMonthlyStatsByChannelId(String profileId, Integer months) {
        log.info("=== Getting Monthly Stats for Channel: {} (Last {} months) ===", profileId, months);

        // Validate and set default months
        if (months == null || months <= 0) {
            months = 6;
        }

        // Calculate start and end dates for the period
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(months);

        // Get actual payment date range (may be null if no payments)
        Object[] dateRange = null;
        LocalDateTime firstPaymentDate = startDate;
        LocalDateTime lastPaymentDate = endDate;

        try {
            dateRange = paymentRepository.getPaymentDateRange(profileId);
            if (dateRange != null && dateRange.length >= 2) {
                if (dateRange[0] != null) {
                    firstPaymentDate = (LocalDateTime) dateRange[0];
                }
                if (dateRange[1] != null) {
                    lastPaymentDate = (LocalDateTime) dateRange[1];
                }
            }
            log.info("Payment date range: {} to {}", firstPaymentDate, lastPaymentDate);
        } catch (Exception e) {
            log.warn("Error getting payment date range: {}", e.getMessage());
        }

        // Initialize map to store monthly stats
        Map<String, MonthlyStatsDTO> monthlyStatsMap = new HashMap<>();

        // Step 1: Get monthly revenue data
        try {
            List<Object[]> revenueData = paymentRepository.getMonthlyRevenueByChannelId(profileId);
            if (revenueData != null && !revenueData.isEmpty()) {
                log.info("Found {} months of revenue data", revenueData.size());

                for (Object[] row : revenueData) {
                    try {
                        Integer year = row[0] != null ? (Integer) row[0] : null;
                        Integer month = row[1] != null ? (Integer) row[1] : null;
                        Long paymentCount = row[2] != null ? (Long) row[2] : 0L;
                        BigDecimal revenue = row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO;

                        if (year != null && month != null) {
                            String monthKey = String.format("%d-%02d", year, month);
                            YearMonth yearMonth = YearMonth.of(year, month);

                            MonthlyStatsDTO stats = MonthlyStatsDTO.builder()
                                    .month(yearMonth.format(MONTH_FORMATTER))
                                    .year(year)
                                    .monthNumber(month)
                                    .revenue(revenue)
                                    .newPayments(paymentCount)
                                    .newMembers(0L)
                                    .totalMembers(0L)
                                    .cumulativeRevenue(BigDecimal.ZERO)
                                    .build();

                            monthlyStatsMap.put(monthKey, stats);
                        }
                    } catch (Exception e) {
                        log.error("Error processing revenue row: {}", e.getMessage());
                    }
                }
            } else {
                log.info("No revenue data found for profile: {}", profileId);
            }
        } catch (Exception e) {
            log.error("Error getting monthly revenue: {}", e.getMessage());
        }

        // Step 2: Get monthly new members data
        try {
            List<Object[]> memberData = membershipRepository.getMonthlyNewMembersByChannelId(profileId);
            if (memberData != null && !memberData.isEmpty()) {
                log.info("Found {} months of member data", memberData.size());

                for (Object[] row : memberData) {
                    try {
                        Integer year = row[0] != null ? (Integer) row[0] : null;
                        Integer month = row[1] != null ? (Integer) row[1] : null;
                        Long newMembers = row[2] != null ? (Long) row[2] : 0L;

                        if (year != null && month != null) {
                            String monthKey = String.format("%d-%02d", year, month);
                            YearMonth yearMonth = YearMonth.of(year, month);

                            MonthlyStatsDTO stats = monthlyStatsMap.get(monthKey);
                            if (stats != null) {
                                stats.setNewMembers(newMembers);
                            } else {
                                stats = MonthlyStatsDTO.builder()
                                        .month(yearMonth.format(MONTH_FORMATTER))
                                        .year(year)
                                        .monthNumber(month)
                                        .newMembers(newMembers)
                                        .revenue(BigDecimal.ZERO)
                                        .newPayments(0L)
                                        .totalMembers(0L)
                                        .cumulativeRevenue(BigDecimal.ZERO)
                                        .build();
                                monthlyStatsMap.put(monthKey, stats);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error processing member row: {}", e.getMessage());
                    }
                }
            } else {
                log.info("No member data found for profile: {}", profileId);
            }
        } catch (Exception e) {
            log.error("Error getting monthly members: {}", e.getMessage());
        }

        // Step 3: Fill in missing months and calculate cumulative values
        List<MonthlyStatsDTO> completeMonthlyData = new ArrayList<>();
        YearMonth currentYearMonth = YearMonth.from(startDate);
        YearMonth endYearMonth = YearMonth.from(endDate);

        BigDecimal cumulativeRevenue = BigDecimal.ZERO;
        Long cumulativeMembers = 0L;

        while (!currentYearMonth.isAfter(endYearMonth)) {
            String monthKey = String.format("%d-%02d",
                    currentYearMonth.getYear(),
                    currentYearMonth.getMonthValue());

            MonthlyStatsDTO stats = monthlyStatsMap.get(monthKey);

            if (stats == null) {
                // Create empty stats for missing months
                stats = MonthlyStatsDTO.builder()
                        .month(currentYearMonth.format(MONTH_FORMATTER))
                        .year(currentYearMonth.getYear())
                        .monthNumber(currentYearMonth.getMonthValue())
                        .newMembers(0L)
                        .revenue(BigDecimal.ZERO)
                        .newPayments(0L)
                        .build();
            }

            // Calculate cumulative values
            cumulativeRevenue = cumulativeRevenue.add(
                    stats.getRevenue() != null ? stats.getRevenue() : BigDecimal.ZERO
            );
            cumulativeMembers += stats.getNewMembers() != null ? stats.getNewMembers() : 0L;

            stats.setCumulativeRevenue(cumulativeRevenue);
            stats.setTotalMembers(cumulativeMembers);

            completeMonthlyData.add(stats);
            currentYearMonth = currentYearMonth.plusMonths(1);
        }

        // Step 4: Get overall stats
        MembershipStatsDTO overallStats = getChannelMembershipStats(profileId);

        log.info("Generated {} months of data, total revenue: {}, total members: {}",
                completeMonthlyData.size(),
                cumulativeRevenue,
                cumulativeMembers);

        return MembershipMonthlyStatsResponse.builder()
                .monthlyData(completeMonthlyData)
                .overallStats(overallStats)
                .totalMonths(completeMonthlyData.size())
                .startDate(firstPaymentDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .endDate(lastPaymentDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build();
    }

    public MembershipMonthlyStatsResponse getMonthlyStatsByChannelId(String profileId) {
        return getMonthlyStatsByChannelId(profileId, 6);
    }

}