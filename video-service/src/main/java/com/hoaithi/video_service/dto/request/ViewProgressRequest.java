package com.hoaithi.video_service.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewProgressRequest {
    private String sessionId;
    private Double watchDuration;
    private Double watchPercentage;
}