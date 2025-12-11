package com.hoaithi.event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUnsubscribedEvent {
    String unsubscriberId;
    String channelId;
}
