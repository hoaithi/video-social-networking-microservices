package com.hoaithi.notification_service.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {
    @Id
    private String id;

    private String subscriberId;   // Ai là người subscribe
    private String channelId;   // Người được subscribe
}
