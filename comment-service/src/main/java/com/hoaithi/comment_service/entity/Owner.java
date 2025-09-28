package com.hoaithi.comment_service.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Owner {
    String profileId;
    String avatarUrl;
    String fullName;
}
