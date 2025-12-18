package com.hoaithi.video_service.repository.httpclient;

import com.hoaithi.video_service.configuration.AuthenticationRequestIntercepter;
import com.hoaithi.video_service.dto.response.ApiResponse;
import com.hoaithi.video_service.dto.response.ProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "profile-service", url = "http://localhost:8081",
        configuration = AuthenticationRequestIntercepter.class)
public interface ProfileClient {

    @GetMapping("/profile/{id}")
    ApiResponse<ProfileResponse> getProfileById(@PathVariable String id);

    @GetMapping("/memberships/check")
    ApiResponse<Boolean>checkMembership(@RequestParam String channelId) ;


    @GetMapping("/subscription/{channelId}/count")
    ApiResponse<Long> getSubscriberCount(@PathVariable("channelId") String channelId);

    @GetMapping("/profile/count/total")
    ApiResponse<Long> getTotalUserCount();

    @GetMapping("/profile/admin/daily-registrations")
    ApiResponse<List<Object[]>> getDailyUserRegistrations(
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate);

    @GetMapping("/profile/admin/monthly-registrations")
    ApiResponse<List<Object[]>> getMonthlyUserRegistrations(
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate);

    @GetMapping("/profile/admin/count-period")
    ApiResponse<Long> countUsersInPeriod(
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate);
}
