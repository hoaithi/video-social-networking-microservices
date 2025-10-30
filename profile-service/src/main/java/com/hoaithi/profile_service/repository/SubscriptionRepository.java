package com.hoaithi.profile_service.repository;

import com.hoaithi.profile_service.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
    boolean existsByUserIdAndChannelId(String userProfileId, String channelProfileId);

    Long countByChannelId(String channelProfileId);

    Optional<Subscription> findByUserIdAndChannelId(String userId, String channelId);

    List<Subscription> findByUserId(String userId);
}
