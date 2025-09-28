package com.hoaithi.profile_service.controller;


import com.hoaithi.profile_service.dto.request.ProfileRequest;
import com.hoaithi.profile_service.dto.response.ApiResponse;
import com.hoaithi.profile_service.dto.response.ProfileResponse;
import com.hoaithi.profile_service.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Tag(name = "Profile internal API", description = "Manage interact among services ")
public class InternalProfileController {
    ProfileService profileService;

    @PostMapping("/internal/users")
    @Operation(
            summary = "Create a new user profile (internal use)",
            description = """
        This endpoint is called by the Identity Service after a new user account 
        has been successfully created.  
        
        It creates a corresponding profile record in the Profile Service.  
        This API is **internal only** and should not be exposed directly to external clients.
        """
    )
    public ApiResponse<ProfileResponse> createProfile(@RequestBody ProfileRequest profileRequest) {
        ProfileResponse created = profileService.createProfile(profileRequest);
        return ApiResponse.<ProfileResponse>builder()
                .result(created)
                .message("create profile successful")
                .build();
    }
    @GetMapping("/internal/users/{userId}")
    @Operation(
            summary = "Get profile by userId",
            description = "Retrieve detailed profile information of a user by userId"
    )
    public ApiResponse<ProfileResponse> getProfileByUserId(
            @Parameter(description = "The userId of the user profile")
            @PathVariable String userId) {
        ProfileResponse profile = profileService.getProfileByUserId(userId);
        return ApiResponse.<ProfileResponse>builder()
                .message("Profile retrieved successfully")
                .result(profile)
                .build();
    }




}
