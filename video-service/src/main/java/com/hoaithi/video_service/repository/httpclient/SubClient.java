package com.hoaithi.video_service.repository.httpclient;

import com.hoaithi.video_service.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "profile-service-sub", url = "http://localhost:8081/subscription")
public interface SubClient {

    @GetMapping("/{channelId}/count")
    ApiResponse<Long> getSubscriberCount(@PathVariable("channelId") String channelId);
}
