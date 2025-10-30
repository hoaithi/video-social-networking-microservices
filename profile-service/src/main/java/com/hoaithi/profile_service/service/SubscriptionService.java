package com.hoaithi.profile_service.service;

import com.hoaithi.profile_service.dto.response.ProfileResponse;
import com.hoaithi.profile_service.dto.response.SubscriptionResponse;
import com.hoaithi.profile_service.entity.Profile;
import com.hoaithi.profile_service.entity.Subscription;
import com.hoaithi.profile_service.exception.AppException;
import com.hoaithi.profile_service.exception.ErrorCode;
import com.hoaithi.profile_service.repository.ProfileRepository;
import com.hoaithi.profile_service.repository.SubscriptionRepository;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Service
public class SubscriptionService {
    ProfileRepository profileRepository;
    SubscriptionRepository subscriptionRepository;


    public boolean subscribe(String channelId){
        String userId = getCurrentUserId();
        Profile userProfile = profileRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_EXISTED));
        Profile channelProfile = profileRepository.findById(channelId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_EXISTED));
        if(userId.equals(channelId))
            throw new AppException(ErrorCode.USER_CHANNEL_SAME);

        if(!isSubscribed(channelId)){
            subscriptionRepository.save(Subscription.builder()
                    .user(userProfile)
                    .channel(channelProfile)
                    .build());
        }
        return isSubscribed(channelId);
    }
    public void unsubscribe(String channelId){
        String userId = getCurrentUserId();
        Subscription subscription = subscriptionRepository.findByUserIdAndChannelId(userId, channelId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_EXISTED));
        subscriptionRepository.delete(subscription);
    }

    public boolean isSubscribed(String channelProfileId){
        String userId = getCurrentUserId();
        return subscriptionRepository.existsByUserIdAndChannelId(userId, channelProfileId);
    }
    public Long countSubscription(String channelProfileId){
        return subscriptionRepository.countByChannelId(channelProfileId);
    }



    public void toggleNotification(String channelId, boolean enabled) {
        String userId = getCurrentUserId();
        Subscription subscription = subscriptionRepository.findByUserIdAndChannelId(userId, channelId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_EXISTED));

        subscription.setNotificationEnabled(enabled);
        subscriptionRepository.save(subscription);
    }

    public List<SubscriptionResponse> getProfileSubscriptions(){
        String userId = getCurrentUserId();
        return subscriptionRepository.findByUserId(userId)
                .stream()
                .map(this::mapToSubscriptionDTO)
                .toList();
    }

    private String getCurrentUserId(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private SubscriptionResponse mapToSubscriptionDTO(Subscription subscription) {
        ProfileResponse channelDTO = ProfileResponse.builder()
                .id(subscription.getChannel().getId())
                .fullName(subscription.getChannel().getFullName())
                .avatarUrl(subscription.getChannel().getAvatarUrl())
                .build();

        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .channel(channelDTO)
                .subscribedAt(subscription.getSubscribedAt())
                .notificationEnabled(subscription.isNotificationEnabled())
                .build();
    }

}
