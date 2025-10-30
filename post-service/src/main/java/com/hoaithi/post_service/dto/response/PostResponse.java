package com.hoaithi.post_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PostResponse {
    String id;
    String profileId;
    String profileImage;
    String profileName;
    String content;
    String title;
    String imageUrl;
    Long heartCount;
    Instant createdAt;
}
