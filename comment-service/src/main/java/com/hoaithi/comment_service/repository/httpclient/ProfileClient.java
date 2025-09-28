package com.hoaithi.comment_service.repository.httpclient;

import com.hoaithi.comment_service.configuration.AuthenticationRequestIntercepter;
import com.hoaithi.comment_service.dto.response.ApiResponse;
import com.hoaithi.comment_service.dto.response.ProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "profile-service", url = "http://localhost:8081/profile", configuration = AuthenticationRequestIntercepter.class)
public interface ProfileClient {
    @GetMapping("/users/my-profile")
    ApiResponse<ProfileResponse> getMyProfile();
}
