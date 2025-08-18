package com.hoaithi.identity_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileRequest {
    String firstName;
    String lastName;
    LocalDate dob;
    String city;
    String userId;
    String email;
}

