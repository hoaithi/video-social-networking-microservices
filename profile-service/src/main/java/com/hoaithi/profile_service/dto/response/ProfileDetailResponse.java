package com.hoaithi.profile_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDetailResponse {
    private String id;
    private String userId;
    private String fullName;
    private String dob;
    private String city;
    private String avatarUrl;
    private String bannerUrl;
    private String email;
    private String description;
    private boolean hasPassword;
    private Long subscriberCount;
    private Long subscribingCount;
    private String createdAt;
}