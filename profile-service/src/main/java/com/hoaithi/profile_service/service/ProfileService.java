package com.hoaithi.profile_service.service;


import com.hoaithi.profile_service.dto.request.ProfileRequest;
import com.hoaithi.profile_service.dto.request.UpdateProfileRequest;
import com.hoaithi.profile_service.dto.response.PagedResponse;
import com.hoaithi.profile_service.dto.response.ProfileDetailResponse;
import com.hoaithi.profile_service.dto.response.ProfileResponse;
import com.hoaithi.profile_service.dto.response.UpdateProfileResponse;
import com.hoaithi.profile_service.entity.Profile;
import com.hoaithi.profile_service.exception.AppException;
import com.hoaithi.profile_service.exception.ErrorCode;
import com.hoaithi.profile_service.mapper.ProfileMapper;
import com.hoaithi.profile_service.repository.ProfileRepository;
import com.hoaithi.profile_service.repository.SubscriptionRepository;
import com.hoaithi.profile_service.repository.httpclient.FileClient;
import com.hoaithi.profile_service.repository.httpclient.VideoClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProfileService {

    ProfileRepository profileRepository;
    ProfileMapper profileMapper;
    FileClient fileClient;
    VideoClient videoClient;

    SubscriptionRepository subscriptionRepository;

    public ProfileResponse createProfile(ProfileRequest profileRequest) {
        Profile profile = profileMapper.toProfile(profileRequest);
        return profileMapper.toProfileResponse(profileRepository.save(profile));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<ProfileResponse> getAllProfiles() {
        List<Profile> profiles = profileRepository.findAll();
        return profiles.stream()
                .map(profileMapper::toProfileResponse)
                .toList();
    }

    public ProfileResponse getProfileById(String id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id " + id));
        return profileMapper.toProfileResponse(profile);
    }

    public ProfileResponse updateProfile(String id, UpdateProfileRequest profileRequest, MultipartFile avatar, MultipartFile banner) {
        log.info("id:" + id);
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_EXISTED));
        if(avatar != null){
            profile.setAvatarUrl(fileClient.uploadFile(avatar));
        }
        if(banner != null){
            profile.setBannerUrl(fileClient.uploadFile(banner));
        }
        if(profileRequest != null){
            if (profileRequest.getFullName() != null) {
                profile.setFullName(profileRequest.getFullName());
            }
            if (profileRequest.getDob() != null) {
                profile.setDob(profileRequest.getDob());
            }
            if (profileRequest.getCity() != null) {
                profile.setCity(profileRequest.getCity());
            }
            if (profileRequest.getDescription() != null){
                profile.setDescription(profileRequest.getDescription());
            }
        }

        profile = profileRepository.save(profile);
        return profileMapper.toProfileResponse(profile);
    }

    public ProfileResponse getMyFile() {
        String profileId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info(profileId);
        Profile profile = profileRepository.findById(profileId).orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_EXISTED));
        return profileMapper.toProfileResponse(profile);
    }

    public ProfileResponse getProfileByUserId(String userId) {
        Profile profile = profileRepository.findByUserId(userId);
        return profileMapper.toProfileResponse(profile);
    }

    public void updateHasPassword(ProfileRequest request){
        Profile profile = profileRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_EXISTED));
        profile.setHasPassword(true);
        profileRepository.save(profile);
    }

    public PagedResponse<ProfileDetailResponse> getAllProfilesForAdmin(
            int page, int size, String search, Boolean hasPassword, String sortBy, String sortDirection) {

        log.info("=== Getting All Profiles - Filters: search={}, hasPassword={}, sort={} {} ===",
                search, hasPassword, sortBy, sortDirection);

        // Build Sort
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);

        // Get profiles with filters
        Page<Profile> profilePage;

        if (search != null && !search.isEmpty() && hasPassword != null) {
            // Filter by both search and hasPassword
            profilePage = profileRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndHasPassword(
                    search, search, hasPassword, pageable);
            log.info("Applied filters: search + hasPassword");
        } else if (search != null && !search.isEmpty()) {
            // Filter by search only (fullName or email)
            profilePage = profileRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    search, search, pageable);
            log.info("Applied filter: search only");
        } else if (hasPassword != null) {
            // Filter by hasPassword only
            profilePage = profileRepository.findByHasPassword(hasPassword, pageable);
            log.info("Applied filter: hasPassword only");
        } else {
            // No filters
            profilePage = profileRepository.findAll(pageable);
            log.info("No filters applied");
        }

        log.info("Found {} profiles on page {} of {}",
                profilePage.getContent().size(), page, profilePage.getTotalPages());

        // Map to ProfileDetailResponse with additional stats
        List<ProfileDetailResponse> profileResponses = profilePage.getContent().stream()
                .map(profile -> {
                    // Get subscriber count
                    Long subscriberCount = subscriptionRepository.countByChannelId(profile.getId());

                    // Get subscribing count (channels this user follows)
                    Long subscribingCount = subscriptionRepository.countByUserId(profile.getId());

                    // ✅ GET VIDEO COUNT
                    Long totalVideos = 0L;
                    try {
                        totalVideos = videoClient.getVideoCountByProfile(profile.getId()).getResult();
                        log.info("Profile {}: {} videos", profile.getId(), totalVideos);
                    } catch (Exception e) {
                        log.error("Error getting video count for profile {}: {}",
                                profile.getId(), e.getMessage());
                    }

                    return ProfileDetailResponse.builder()
                            .id(profile.getId())
                            .userId(profile.getUserId())
                            .fullName(profile.getFullName())
                            .dob(profile.getDob() != null ? profile.getDob().toString() : null)
                            .city(profile.getCity())
                            .avatarUrl(profile.getAvatarUrl())
                            .bannerUrl(profile.getBannerUrl())
                            .email(profile.getEmail())
                            .description(profile.getDescription())
                            .hasPassword(profile.isHasPassword())
                            .subscriberCount(subscriberCount)
                            .subscribingCount(subscribingCount)
                            .totalVideos(totalVideos)
                            .createdAt(profile.getCreatedAt().toString())
                            .build();
                })
                .toList();

        PagedResponse<ProfileDetailResponse> response = PagedResponse.<ProfileDetailResponse>builder()
                .content(profileResponses)
                .page(profilePage.getNumber())
                .size(profilePage.getSize())
                .totalElements(profilePage.getTotalElements())
                .totalPages(profilePage.getTotalPages())
                .last(profilePage.isLast())
                .build();

        return response;
    }

    public Long getTotalUserCount() {
        log.info("Getting total user count");
        Long count = profileRepository.count();
        log.info("Total users: {}", count);
        return count;
    }

    // Thêm các methods này vào ProfileService.java

    public List<Object[]> getDailyUserRegistrations(LocalDate startDate, LocalDate endDate) {
        log.info("Getting daily user registrations from {} to {}", startDate, endDate);
        return profileRepository.getDailyUserRegistrations(startDate, endDate);
    }

    public List<Object[]> getMonthlyUserRegistrations(LocalDate startDate, LocalDate endDate) {
        log.info("Getting monthly user registrations from {} to {}", startDate, endDate);
        return profileRepository.getMonthlyUserRegistrations(startDate, endDate);
    }

    public Long countUsersInPeriod(LocalDate startDate, LocalDate endDate) {
        log.info("Counting users in period from {} to {}", startDate, endDate);
        return profileRepository.countUsersInPeriod(startDate, endDate);
    }

    public void deleteProfile(String id) {
        log.info("=== Deleting Profile with ID: {} ===", id);

        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_EXISTED));

        profileRepository.delete(profile);

        log.info("=== Profile {} deleted successfully ===", id);
    }
}
