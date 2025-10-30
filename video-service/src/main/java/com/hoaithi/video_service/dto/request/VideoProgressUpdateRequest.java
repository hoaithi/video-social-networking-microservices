package com.hoaithi.video_service.dto.request;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VideoProgressUpdateRequest {
    long currentTime;
    double duration;
    Boolean isCompleted;
}
