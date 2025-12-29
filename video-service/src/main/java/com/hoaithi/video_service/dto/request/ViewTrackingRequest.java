
package com.hoaithi.video_service.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewTrackingRequest {
    private String sessionId;
    private Double watchDuration;
    private Double watchPercentage;

    @Builder.Default
    private Boolean hasInteracted = false;

    private String ipAddress;
    private String userAgent;
}