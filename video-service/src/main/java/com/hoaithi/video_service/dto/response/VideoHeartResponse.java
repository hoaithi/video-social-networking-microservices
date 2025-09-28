package com.hoaithi.video_service.dto.response;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class VideoHeartResponse {
    String videoId;
    Long heartCount;
    boolean hearted;
}
