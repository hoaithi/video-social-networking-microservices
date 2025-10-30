package com.hoaithi.profile_service.controller;

import com.hoaithi.profile_service.dto.request.SubscribeRequest;
import com.hoaithi.profile_service.dto.response.ApiResponse;
import com.hoaithi.profile_service.dto.response.SubscriptionResponse;
import com.hoaithi.profile_service.service.SubscriptionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController()
@RequestMapping("/subscription")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SubscriptionController {

    SubscriptionService subscriptionService;

    @PostMapping("/{channelId}")
    public ApiResponse<?> subscribe(@PathVariable String channelId){
        boolean isSubscribe = subscriptionService.subscribe(channelId);
        return ApiResponse.<Boolean>builder()
                .message("Subscribe successfully")
                .result(isSubscribe)
                .build();
    }
    @PutMapping("/{channelId}/notifications")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> toggleNotifications(
            @PathVariable String channelId,
            @RequestParam boolean enabled) {

        subscriptionService.toggleNotification(channelId, enabled);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Notification settings updated")
                .build());
    }

    @DeleteMapping("/{channelId}")
    public ApiResponse<Void> unsubscribe(@PathVariable String channelId){
        subscriptionService.unsubscribe(channelId);
        return ApiResponse.<Void>builder()
                .message("Subscribe successfully")
                .build();
    }
    @GetMapping("/check")
    public ApiResponse<?> checkSubscription(@RequestParam String channelId){
        return ApiResponse.<Boolean>builder()
                .result(subscriptionService.isSubscribed(channelId))
                .build();
    }

    @GetMapping("/{channelId}/count")
    public ApiResponse<?> countSubscription(@PathVariable String channelId){
        return ApiResponse.<Long>builder()
                .result(subscriptionService.countSubscription(channelId))
                .build();
    }
    @GetMapping("/profile")
    public ApiResponse<?> getChannelSubscriptions(){
        return ApiResponse.<List<SubscriptionResponse>>builder()
                .result(subscriptionService.getProfileSubscriptions())
                .build();
    }

}
