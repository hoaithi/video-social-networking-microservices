package com.hoaithi.profile_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private String id;
    private LocalDate subscribedAt;
    private boolean notificationEnabled = true;
    private ProfileResponse user;
    private ProfileResponse channel;
}

