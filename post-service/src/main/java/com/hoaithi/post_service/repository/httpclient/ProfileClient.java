package com.hoaithi.post_service.repository.httpclient;

import com.hoaithi.post_service.dto.response.ApiResponse;
import com.hoaithi.post_service.dto.response.ProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "profile-service", url = "http://localhost:8081/profile")
public interface ProfileClient {

    @GetMapping("/{id}")
    ApiResponse<ProfileResponse> getProfileById(@PathVariable String id);
}
