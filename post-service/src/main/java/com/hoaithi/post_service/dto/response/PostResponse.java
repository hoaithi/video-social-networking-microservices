package com.hoaithi.post_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PostResponse {
    String id;
    String profileId;
    String content;
    String imageUrl;
    Long heartCount;
}
