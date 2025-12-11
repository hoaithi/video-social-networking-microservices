package com.hoaithi.event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSubscribedEvent {
    String subscriberId;
    String channelId;
    String fullName;
    String avatarUrl;
}
