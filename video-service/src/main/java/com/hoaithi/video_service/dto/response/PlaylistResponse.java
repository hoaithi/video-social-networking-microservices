package com.hoaithi.video_service.dto.response;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PlaylistResponse {
    String name;
    String id;
    int videoCount;
}
