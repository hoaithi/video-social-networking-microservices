package com.hoaithidev.vidsonet_backend.dto.membership;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembershipTierUpdateRequest {
    private String name;

    private String description;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @Min(value = 1, message = "Duration must be at least 1 month")
    private Integer durationMonths;

    @JsonProperty("isActive")
    private Boolean isActive;
}