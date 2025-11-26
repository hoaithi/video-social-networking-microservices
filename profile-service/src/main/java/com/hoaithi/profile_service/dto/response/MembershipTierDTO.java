package com.hoaithidev.vidsonet_backend.dto.membership;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipTierDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationMonths;
    @JsonProperty("isActive")
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long channelId;
    private String channelName;
}