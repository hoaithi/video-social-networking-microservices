package com.hoaithi.identity_service.repository.httpclient;

import com.hoaithi.identity_service.dto.request.ApiResponse;
import com.hoaithi.identity_service.dto.response.ProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
@FeignClient(name = "profile-service", url = "http://localhost:8081")
public interface ProfileClient {
    @PostMapping("/internal/profile")
    public ApiResponse<ProfileResponse> createProfile(@RequestBody Object profileRequest);

    @GetMapping("/internal/profile/{userId}")
    public ApiResponse<ProfileResponse> getProfileByUserId(@PathVariable String userId);

}
