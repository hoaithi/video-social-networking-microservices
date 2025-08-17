package com.hoaithi.api_gateway.repository;


import com.hoaithi.api_gateway.dto.request.IntrospectRequest;
import com.hoaithi.api_gateway.dto.response.ApiResponse;
import com.hoaithi.api_gateway.dto.response.IntrospectResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

@Repository
public interface IdentityClient {
    @PostExchange(value = "/auth/introspect",
                  contentType = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ApiResponse<IntrospectResponse>> introspect(@RequestBody IntrospectRequest request);

}
