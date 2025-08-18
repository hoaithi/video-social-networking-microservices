package com.hoaithi.identity_service.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
@FeignClient(name = "profile-service", url = "http://localhost:8081/profile")
public interface ProfileClient {

    @PostMapping("/internal/users")
    public Object createProfile(@RequestBody Object profileRequest);
}
