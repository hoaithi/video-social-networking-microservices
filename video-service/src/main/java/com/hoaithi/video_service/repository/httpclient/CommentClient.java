package com.hoaithi.video_service.repository.httpclient;

import com.hoaithi.video_service.dto.response.ApiResponse;
import com.hoaithi.video_service.dto.response.CommentCountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "comment-service", url = "http://localhost:8086/comment")
public interface CommentClient {
    @GetMapping("/internal/{itemId}/count")
    public ApiResponse<CommentCountResponse> countComment(@PathVariable String itemId);
}
