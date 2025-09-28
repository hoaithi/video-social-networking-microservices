package com.hoaithi.comment_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {
    String id;
    String userId;
    String fullName;
    LocalDate dob;
    String city;
    String avatarUrl;
    String bannerUrl;
}