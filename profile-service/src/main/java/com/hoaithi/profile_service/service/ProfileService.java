package com.hoaithi.profile_service.service;


import com.hoaithi.profile_service.dto.request.ProfileRequest;
import com.hoaithi.profile_service.dto.request.UpdateProfileRequest;
import com.hoaithi.profile_service.dto.response.ProfileResponse;
import com.hoaithi.profile_service.dto.response.UpdateProfileResponse;
import com.hoaithi.profile_service.entity.Profile;
import com.hoaithi.profile_service.exception.AppException;
import com.hoaithi.profile_service.exception.ErrorCode;
import com.hoaithi.profile_service.mapper.ProfileMapper;
import com.hoaithi.profile_service.repository.ProfileRepository;
import com.hoaithi.profile_service.repository.httpclient.FileClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProfileService {

    ProfileRepository profileRepository;
    ProfileMapper profileMapper;
    FileClient fileClient;

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

    public UpdateProfileResponse updateProfile(String id, UpdateProfileRequest profileRequest, MultipartFile avatar, MultipartFile banner) {
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
        }

        profile = profileRepository.save(profile);
        return profileMapper.toUpdateProfileResponse(profile);
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
}
