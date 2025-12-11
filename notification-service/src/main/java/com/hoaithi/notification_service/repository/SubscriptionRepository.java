package com.hoaithi.notification_service.repository;

import com.hoaithi.notification_service.entity.Subscription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
    void deleteBySubscriberIdAndChannelId(String subscriberId, String channelId);
    List<Subscription> findAllByChannelId(String channelId);
}
