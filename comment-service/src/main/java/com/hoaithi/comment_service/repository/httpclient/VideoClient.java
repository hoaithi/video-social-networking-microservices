package com.hoaithi.comment_service.repository.httpclient;

import com.hoaithi.comment_service.configuration.AuthenticationRequestIntercepter;
import com.hoaithi.comment_service.dto.response.ApiResponse;
import com.hoaithi.comment_service.dto.response.OwnerIdResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "video-service", url = "http://localhost:8085", configuration = AuthenticationRequestIntercepter.class)
public interface VideoClient {

    @GetMapping("/internal/video/{videoId}")
    public OwnerIdResponse getOwnerById(@PathVariable("videoId") String videoId);
}
