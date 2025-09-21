package com.hoaithi.profile_service.service;


import com.hoaithi.profile_service.dto.request.ProfileRequest;
import com.hoaithi.profile_service.dto.request.UpdateProfileRequest;
import com.hoaithi.profile_service.dto.response.ProfileResponse;
import com.hoaithi.profile_service.dto.response.UpdateProfileResponse;
import com.hoaithi.profile_service.entity.Profile;
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
                .orElseThrow(() -> new RuntimeException("Profile not found with id " + id));
        if(!avatar.isEmpty()){
            profile.setAvatarUrl(fileClient.uploadFile(avatar));
        }
        if(!banner.isEmpty()){
            profile.setBannerUrl(fileClient.uploadFile(banner));
        }
        if (profileRequest.getFirstName() != null) {
            profile.setFirstName(profileRequest.getFirstName());
        }
        if (profileRequest.getLastName() != null) {
            profile.setLastName(profileRequest.getLastName());
        }
        if (profileRequest.getDob() != null) {
            profile.setDob(profileRequest.getDob());
        }
        if (profileRequest.getCity() != null) {
            profile.setCity(profileRequest.getCity());
        }
        profile = profileRepository.save(profile);
        return profileMapper.toUpdateProfileResponse(profile);
    }


    public ProfileResponse getMyFile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Profile profile = profileRepository.findByUserId(authentication.getName());
        return profileMapper.toProfileResponse(profile);
    }
}
