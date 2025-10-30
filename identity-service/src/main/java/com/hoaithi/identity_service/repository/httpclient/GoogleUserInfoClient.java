package com.hoaithi.identity_service.repository.httpclient;

import com.hoaithi.identity_service.dto.response.GoogleUserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "google-user-info-client", url = "https://www.googleapis.com")
public interface GoogleUserInfoClient {
    @GetMapping("/oauth2/v1/userinfo")
    public GoogleUserInfoResponse getUserInfo(@RequestParam("alt") String alt,
                                              @RequestParam("access_token") String accessToken);
}
