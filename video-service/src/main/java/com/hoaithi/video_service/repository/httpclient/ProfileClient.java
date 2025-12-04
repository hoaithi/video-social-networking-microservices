package com.hoaithi.video_service.repository.httpclient;

import com.hoaithi.video_service.dto.response.ApiResponse;
import com.hoaithi.video_service.dto.response.ProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "profile-service", url = "http://localhost:8081/profile")
public interface ProfileClient {

    @GetMapping("/{id}")
    ApiResponse<ProfileResponse> getProfileById(@PathVariable String id);

    @GetMapping("/subscription/{channelId}/count")
    ApiResponse<Long> getSubscriberCount(@PathVariable("channelId") String channelId);

    @GetMapping("/count/total")
    ApiResponse<Long> getTotalUserCount();
}
