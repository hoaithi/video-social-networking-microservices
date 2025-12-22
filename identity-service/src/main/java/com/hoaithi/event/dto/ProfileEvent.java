package com.hoaithi.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileEvent {
    String fullName;
    LocalDate dob;
    String city;
    String userId;
    String email;
    String avatarUrl;
    boolean hasPassword;
}

