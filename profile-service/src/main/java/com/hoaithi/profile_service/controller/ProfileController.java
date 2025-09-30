package com.hoaithi.profile_service.controller;

import com.hoaithi.profile_service.dto.request.ProfileRequest;
import com.hoaithi.profile_service.dto.request.UpdateProfileRequest;
import com.hoaithi.profile_service.dto.response.ApiResponse;
import com.hoaithi.profile_service.dto.response.ProfileResponse;
import com.hoaithi.profile_service.dto.response.UpdateProfileResponse;
import com.hoaithi.profile_service.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController()
@RequestMapping("/profile")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Tag(name = "Profile API", description = "Manage the user information")
public class ProfileController {
    ProfileService profileService;

    @GetMapping
    @Operation(
            summary = "Get all user profiles",
            description = "Retrieve a list of all user profiles available in the system."
    )
    public ApiResponse<List<ProfileResponse>> getAllProfiles() {
        return ApiResponse.<List<ProfileResponse>>builder()
                .result(profileService.getAllProfiles())
                .message("List of profiles retrieved successfully")
                .build();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get profile by ID",
            description = "Retrieve detailed profile information of a user by ID"
    )
    public ApiResponse<ProfileResponse> getProfileById(
            @Parameter(description = "The unique ID of the user profile")
            @PathVariable String id) {
        ProfileResponse profile = profileService.getProfileById(id);
        return ApiResponse.<ProfileResponse>builder()
                .message("Profile retrieved successfully")
                .result(profile)
                .build();
    }



    @GetMapping("/my-profile")
    @Operation(
            summary = "Get my profile",
            description = "Retrieve the profile information of the currently logged-in user"
    )
    public ApiResponse<ProfileResponse> getMyProfile(){
        ProfileResponse response = profileService.getMyFile();
        return ApiResponse.<ProfileResponse>builder()
                .message("My profile retrieved successfully")
                .result(response)
                .build();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Update user profile",
            description = "Update user profile information"
    )
    public ApiResponse<UpdateProfileResponse> updateProfile(
            @Parameter(description = "The unique ID of the user profile to update")
            @PathVariable String id,

            @Parameter(description = "Profile details in JSON format")
            @RequestPart(value = "profile", required = false) UpdateProfileRequest request,

            @Parameter(description = "New avatar image file")
            @RequestPart(value = "avatar", required = false) MultipartFile avatar,

            @Parameter(description = "New banner image file")
            @RequestPart(value = "banner", required = false) MultipartFile banner) {
        UpdateProfileResponse updatedProfile = profileService.updateProfile(id, request, avatar, banner);
        return ApiResponse.<UpdateProfileResponse>builder()
                .result(updatedProfile)
                .message("Profile updated successfully")
                .build();
    }

}
