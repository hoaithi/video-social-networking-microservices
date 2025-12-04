package com.hoaithi.profile_service.controller;


import com.hoaithi.profile_service.dto.request.MembershipTierCreateRequest;
import com.hoaithi.profile_service.dto.request.MembershipTierUpdateRequest;
import com.hoaithi.profile_service.dto.response.ApiResponse;
import com.hoaithi.profile_service.dto.response.MembershipDTO;
import com.hoaithi.profile_service.dto.response.MembershipTierDTO;
import com.hoaithi.profile_service.service.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @PostMapping("/tiers")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<MembershipTierDTO>> createMembershipTier(
            @Valid @RequestBody MembershipTierCreateRequest request) {

        MembershipTierDTO tier = membershipService.createMembershipTier(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<MembershipTierDTO>builder()
                        .message("Membership tier created successfully")
                        .result(tier)
                        .build());
    }

    @PutMapping("/tiers/{tierId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<MembershipTierDTO>> updateMembershipTier(
            @PathVariable Long tierId,
            @Valid @RequestBody MembershipTierUpdateRequest request) {

        MembershipTierDTO tier = membershipService.updateMembershipTier(tierId, request);

        return ResponseEntity.ok(ApiResponse.<MembershipTierDTO>builder()
                .message("Membership tier updated successfully")
                .result(tier)
                .build());
    }

    @DeleteMapping("/tiers/{tierId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> deleteMembershipTier(
            @PathVariable Long tierId) {

        membershipService.deleteMembershipTier(tierId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Membership tier deleted successfully")
                .build());
    }

    @GetMapping("/tiers/channel/{channelId}")
    public ResponseEntity<ApiResponse<List<MembershipTierDTO>>> getChannelMembershipTiers(
            @PathVariable String channelId) {

        List<MembershipTierDTO> tiers = membershipService.getChannelMembershipTiers(channelId);

        return ResponseEntity.ok(ApiResponse.<List<MembershipTierDTO>>builder()
                .message("Channel membership tiers retrieved successfully")
                .result(tiers)
                .build());
    }

    @GetMapping("/tiers/{tierId}")
    public ResponseEntity<ApiResponse<MembershipTierDTO>> getMembershipTierById(
            @PathVariable Long tierId) {

        MembershipTierDTO tier = membershipService.getMembershipTierById(tierId);

        return ResponseEntity.ok(ApiResponse.<MembershipTierDTO>builder()
                .message("Membership tier retrieved successfully")
                .result(tier)
                .build());
    }

//    @GetMapping("/user")
//    @PreAuthorize("hasRole('USER')")
//    public ResponseEntity<ApiResponse<List<MembershipDTO>>> getUserMemberships() {
//
//        List<MembershipDTO> memberships = membershipService.getUserMemberships();
//
//        return ResponseEntity.ok(ApiResponse.<List<MembershipDTO>>builder()
//                .message("User memberships retrieved successfully")
//                .data(memberships)
//                .build());
//    }

//    @GetMapping("/channel/{channelId}")
//    @PreAuthorize("@userSecurity.isCurrentUser(#channelId)")
//    public ResponseEntity<ApiResponse<List<MembershipDTO>>> getChannelMembers(
//            @PathVariable Long channelId) {
//
//        List<MembershipDTO> members = membershipService.getChannelMembers(channelId);
//
//        return ResponseEntity.ok(ApiResponse.<List<MembershipDTO>>builder()
//                .message("Channel members retrieved successfully")
//                .data(members)
//                .build());
//    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> checkMembership(
            @RequestParam String channelId) {

        boolean hasMembership = membershipService.hasActiveChannelMembership(channelId);

        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .message("Membership status checked")
                .result(hasMembership)
                .build());
    }
}