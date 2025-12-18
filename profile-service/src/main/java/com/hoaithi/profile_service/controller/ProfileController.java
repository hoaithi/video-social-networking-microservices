package com.hoaithi.profile_service.controller;

import com.hoaithi.profile_service.dto.request.ProfileRequest;
import com.hoaithi.profile_service.dto.request.UpdateProfileRequest;
import com.hoaithi.profile_service.dto.response.*;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    public ApiResponse<ProfileResponse> getMyProfile() {
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
    public ApiResponse<ProfileResponse> updateProfile(
            @Parameter(description = "The unique ID of the user profile to update")
            @PathVariable String id,

            @RequestPart(value = "fullName", required = false) String fullName,
            @RequestPart(value = "city", required = false) String city,
            @RequestPart(value = "dob", required = false) String dob,
            @RequestPart(value = "description", required = false) String description,

            @Parameter(description = "New avatar image file")
            @RequestPart(value = "avatar", required = false) MultipartFile avatar,

            @Parameter(description = "New banner image file")
            @RequestPart(value = "banner", required = false) MultipartFile banner) {


        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName(fullName);
        request.setCity(city);
        request.setDescription(description);

        // ✅ Xử lý chuyển đổi dob (String → LocalDate)
        if (dob != null && !dob.isEmpty()) {
            try {
                // giả định frontend gửi định dạng yyyy-MM-dd
                LocalDate dateOfBirth = LocalDate.parse(dob, DateTimeFormatter.ISO_LOCAL_DATE);
                request.setDob(dateOfBirth);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format for dob. Expected format: yyyy-MM-dd");
            }
        }
        ProfileResponse updatedProfile = profileService.updateProfile(id, request, avatar, banner);
        return ApiResponse.<ProfileResponse>builder()
                .result(updatedProfile)
                .message("Profile updated successfully")
                .build();
    }

    @GetMapping("/count/total")
    public ApiResponse<Long> getTotalUserCount() {
        log.info("=== Getting Total User Count ===");

        Long totalUsers = profileService.getTotalUserCount();

        return ApiResponse.<Long>builder()
                .result(totalUsers)
                .message("Total user count retrieved successfully")
                .build();
    }

    @GetMapping("/admin/all")
    public ApiResponse<PagedResponse<ProfileDetailResponse>> getAllProfiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean hasPassword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        log.info("=== Admin: Getting All Profiles - Page: {}, Size: {}, Search: {}, HasPassword: {}, Sort: {} {} ===",
                page, size, search, hasPassword, sortBy, sortDirection);

        PagedResponse<ProfileDetailResponse> profiles = profileService.getAllProfilesForAdmin(
                page, size, search, hasPassword, sortBy, sortDirection);

        log.info("=== All Profiles Retrieved Successfully ===");
        return ApiResponse.<PagedResponse<ProfileDetailResponse>>builder()
                .result(profiles)
                .message("All profiles retrieved successfully")
                .build();
    }

    // Thêm các methods này vào ProfileController.java

    @GetMapping("/admin/daily-registrations")
    public ApiResponse<List<Object[]>> getDailyUserRegistrations(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        log.info("=== Getting Daily User Registrations: {} to {} ===", startDate, endDate);

        List<Object[]> data = profileService.getDailyUserRegistrations(startDate, endDate);

        return ApiResponse.<List<Object[]>>builder()
                .result(data)
                .message("Daily user registrations retrieved successfully")
                .build();
    }

    @GetMapping("/admin/monthly-registrations")
    public ApiResponse<List<Object[]>> getMonthlyUserRegistrations(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        log.info("=== Getting Monthly User Registrations: {} to {} ===", startDate, endDate);

        List<Object[]> data = profileService.getMonthlyUserRegistrations(startDate, endDate);

        return ApiResponse.<List<Object[]>>builder()
                .result(data)
                .message("Monthly user registrations retrieved successfully")
                .build();
    }

    @GetMapping("/admin/count-period")
    public ApiResponse<Long> countUsersInPeriod(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        log.info("=== Counting Users in Period: {} to {} ===", startDate, endDate);

        Long count = profileService.countUsersInPeriod(startDate, endDate);

        return ApiResponse.<Long>builder()
                .result(count)
                .message("User count for period retrieved successfully")
                .build();
    }

}
