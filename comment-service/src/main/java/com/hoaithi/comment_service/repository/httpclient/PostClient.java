package com.hoaithi.comment_service.repository.httpclient;

import com.hoaithi.comment_service.configuration.AuthenticationRequestIntercepter;
import com.hoaithi.comment_service.dto.response.OwnerIdResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "post-service", url = "http://localhost:8083", configuration = AuthenticationRequestIntercepter.class)
public interface PostClient {

    @GetMapping("/internal/post/{postId}")
    public OwnerIdResponse getOwnerById(@PathVariable String postId);
}
