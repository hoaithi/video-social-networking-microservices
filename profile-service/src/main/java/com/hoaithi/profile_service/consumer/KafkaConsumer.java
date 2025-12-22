

package com.hoaithi.profile_service.consumer;

import com.hoaithi.event.dto.ProfileEvent;
import com.hoaithi.profile_service.dto.request.ProfileRequest;
import com.hoaithi.profile_service.service.ProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaConsumer {
    ProfileService profileService;
    @KafkaListener(topics = "admin-created-topic1", groupId = "profile-group")
    public void createAdminProfile(ProfileEvent event) {
        ProfileRequest request = ProfileRequest.builder()
                .userId(event.getUserId())
                .email(event.getEmail())
                .hasPassword(true)
                .fullName(event.getFullName())
                .build();
        profileService.createProfile(request);
    }



}
