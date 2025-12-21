package com.hoaithi.profile_service.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import com.hoaithi.profile_service.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@FeignClient(name = "video-service", url = "http://localhost:8085/video")
public interface VideoClient {

    @GetMapping("/count/{profileId}")
    ApiResponse<Long> getVideoCountByProfile(@PathVariable("profileId") String profileId);
}
