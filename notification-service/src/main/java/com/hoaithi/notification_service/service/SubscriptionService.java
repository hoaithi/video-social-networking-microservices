package com.hoaithi.notification_service.service;

import com.hoaithi.event.dto.UserSubscribedEvent;
import com.hoaithi.event.dto.UserUnsubscribedEvent;
import com.hoaithi.notification_service.entity.Subscription;
import com.hoaithi.notification_service.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    public void handleSubscribed(UserSubscribedEvent event) {
        Subscription s = Subscription.builder()
                .subscriberId(event.getSubscriberId())
                .channelId(event.getChannelId())
                .build();

        subscriptionRepository.save(s);
    }
    public void handleUnsubscribed(UserUnsubscribedEvent event) {
        subscriptionRepository.deleteBySubscriberIdAndChannelId(
                event.getUnsubscriberId(),
                event.getChannelId()
        );
    }
}
