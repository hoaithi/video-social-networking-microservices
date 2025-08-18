package com.hoaithi.profile_service.controller;


import com.hoaithi.profile_service.dto.request.ProfileRequest;
import com.hoaithi.profile_service.dto.response.ProfileResponse;
import com.hoaithi.profile_service.service.ProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class InternalProfileController {
    ProfileService profileService;

    @PostMapping("/internal/users")
    public ResponseEntity<ProfileResponse> createProfile(@RequestBody ProfileRequest profileRequest) {
        ProfileResponse created = profileService.createProfile(profileRequest);
        return ResponseEntity.ok(created);
    }



}
