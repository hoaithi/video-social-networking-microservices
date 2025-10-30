package com.hoaithi.identity_service.repository.httpclient;

import com.hoaithi.identity_service.dto.request.GoogleExchangeTokenRequest;
import com.hoaithi.identity_service.dto.response.GoogleExchangeTokenResponse;
import feign.QueryMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "google-client", url = "https://oauth2.googleapis.com")
public interface GoogleClient {

    @PostMapping(value = "/token", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public GoogleExchangeTokenResponse getTokenFromGoogle(@QueryMap GoogleExchangeTokenRequest request);

}
