package com.hoaithi.profile_service.controller;

import com.hoaithi.profile_service.dto.request.ProfileRequest;
import com.hoaithi.profile_service.dto.request.UpdateProfileRequest;
import com.hoaithi.profile_service.dto.response.ApiResponse;
import com.hoaithi.profile_service.dto.response.ProfileResponse;
import com.hoaithi.profile_service.dto.response.UpdateProfileResponse;
import com.hoaithi.profile_service.service.ProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController()
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProfileController {
    ProfileService profileService;

    @GetMapping
    public ResponseEntity<List<ProfileResponse>> getAllProfiles() {
        return ResponseEntity.ok(profileService.getAllProfiles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getProfileById(@PathVariable String id) {

        ProfileResponse profile = profileService.getProfileById(id);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UpdateProfileResponse> updateProfile(
            @PathVariable String id,
            @RequestPart(value = "profile", required = false) UpdateProfileRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar,
            @RequestPart(value = "banner", required = false) MultipartFile banner) {
        UpdateProfileResponse updatedProfile = profileService.updateProfile(id, request, avatar, banner);
        return ApiResponse.<UpdateProfileResponse>builder()
                .result(updatedProfile)
                .build();
    }

}
