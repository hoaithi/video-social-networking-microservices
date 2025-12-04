package com.hoaithi.profile_service.service;


import com.hoaithi.profile_service.dto.request.MembershipTierCreateRequest;
import com.hoaithi.profile_service.dto.request.MembershipTierUpdateRequest;
import com.hoaithi.profile_service.dto.response.MembershipTierDTO;
import com.hoaithi.profile_service.entity.MembershipTier;
import com.hoaithi.profile_service.entity.Profile;
import com.hoaithi.profile_service.exception.AppException;
import com.hoaithi.profile_service.exception.ErrorCode;
import com.hoaithi.profile_service.repository.MembershipRepository;
import com.hoaithi.profile_service.repository.MembershipTierRepository;
import com.hoaithi.profile_service.repository.ProfileRepository;
import com.hoaithi.profile_service.utils.ProfileUtil;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class MembershipService {

    MembershipTierRepository membershipTierRepository;
    MembershipRepository membershipRepository;
    ProfileRepository profileRepository;
    ProfileUtil profileUtil;
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
}