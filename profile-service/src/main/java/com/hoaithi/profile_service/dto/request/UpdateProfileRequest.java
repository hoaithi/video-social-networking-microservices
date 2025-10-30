package com.hoaithi.profile_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {
    String id;
    String fullName;
    LocalDate dob;
    String city;
    String description;
    boolean hasPassword;
}
