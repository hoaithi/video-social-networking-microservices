package com.hoaithi.video_service.dto.request;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VideoCreationRequest {
    private String title;
    private String description;
    private boolean isPremium;
}
