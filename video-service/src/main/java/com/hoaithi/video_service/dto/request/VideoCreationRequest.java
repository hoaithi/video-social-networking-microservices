package com.hoaithi.video_service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VideoCreationRequest {
    private String title;
    private String description;
    @JsonProperty("isPremium")
    private boolean isPremium;
}
