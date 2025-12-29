package com.hoaithi.video_service.controller;

import com.hoaithi.video_service.dto.request.VideoCreationRequest;
import com.hoaithi.video_service.dto.request.ViewProgressRequest;
import com.hoaithi.video_service.dto.request.ViewTrackingRequest;
import com.hoaithi.video_service.dto.response.*;
import com.hoaithi.video_service.repository.VideoRepository;
import com.hoaithi.video_service.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/video")
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class VideoController {

    VideoService videoService;
    VideoRepository videoRepository;
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<VideoResponse> createVideo(
            @RequestPart(value = "videoFile", required = false) MultipartFile videoFile,
            @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            @RequestPart(value = "title", required = false) String title,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "isPremium", required = false) String isPremium
    ) throws IOException {
        VideoCreationRequest request = VideoCreationRequest.builder()
                .title(title)
                .description(description)
                .isPremium(Boolean.parseBoolean(isPremium))
                .build();
        VideoResponse response = videoService.createVideo(videoFile, thumbnailFile, request);

        return ApiResponse.<VideoResponse>builder()
                .result(response)
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<PagedResponse<VideoResponse>> searchVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "publishedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isPremium
    ) {
        return ApiResponse.<PagedResponse<VideoResponse>>builder()
                .result(videoService.searchVideos(
                        keyword,
                        isPremium,
                        page,
                        size,
                        sortBy,
                        sortDir
                ))
                .message("Search videos successfully")
                .build();
    }


    @GetMapping("/watch/{id}")
    public ApiResponse<VideoResponse> getVideoById(@PathVariable("id") String id) {
        log.info("video id: " + id);
        VideoResponse response = videoService.getVideoById(id);
        return ApiResponse.<VideoResponse>builder()
                .result(response)
                .build();
    }

    @GetMapping("/{profileId}")
    public ApiResponse<PagedResponse<VideoResponse>> getVideosByProfile(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @PathVariable("profileId") String profileId
    ) {

        return ApiResponse.<PagedResponse<VideoResponse>>builder()
                .result(videoService.getVideoByProfileId(profileId, page, size))
                .message("Retrieve list of video successfully")
                .build();
    }

    @PostMapping("/{videoId}/like")
//    @PreAuthorize("hasRole('USER')")
    public ApiResponse<VideoResponse> likeVideo(
            @PathVariable("videoId") String videoId) {

        VideoResponse response = videoService.likeVideo(videoId);

        return ApiResponse.<VideoResponse>builder()
                .result(response)
                .build();
    }

    @PostMapping("/{videoId}/dislike")
//    @PreAuthorize("hasRole('USER')")
    public ApiResponse<VideoResponse> dislikeVideo(
            @PathVariable("videoId") String videoId) {

        VideoResponse response = videoService.dislikeVideo(videoId);

        return ApiResponse.<VideoResponse>builder()
                .result(response)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteVideoById(@PathVariable("id") String id) {
        log.info("delete video api");
        videoService.deleteVideo(id);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/{videoId}/reaction")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<VideoUserReaction>> getVideoUserReaction(
            @PathVariable String videoId
    ) {
        VideoUserReaction videoUserReaction = videoService.getUserReaction(videoId);
        return ResponseEntity.ok(ApiResponse.<VideoUserReaction>builder()
                .message("Video user reaction retrieved successfully")
                .result(videoUserReaction)
                .build());
    }

    @GetMapping("/my/dashboard/{profileId}")
    public ApiResponse<DashboardResponse> getDashboardByProfileId(@PathVariable String profileId) {
        log.info("=== Getting Dashboard Stats for Profile: {} ===", profileId);

        DashboardResponse dashboardData = videoService.getDashboardStatsByProfile(profileId);

        log.info("=== Dashboard Stats Retrieved Successfully ===");
        return ApiResponse.<DashboardResponse>builder()
                .result(dashboardData)
                .message("Dashboard data retrieved successfully")
                .build();
    }

    @GetMapping("/admin/dashboard")
    public ApiResponse<DashboardResponse> getAdminDashboard() {
        log.info("=== Admin: Getting Overall Dashboard Stats ===");

        DashboardResponse dashboardData = videoService.getAdminDashboardStats();

        log.info("=== Admin Dashboard Stats Retrieved Successfully ===");
        return ApiResponse.<DashboardResponse>builder()
                .result(dashboardData)
                .message("Admin dashboard data retrieved successfully")
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<PagedResponse<VideoResponse>> getAllVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isPremium,
            @RequestParam(defaultValue = "publishedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        log.info("=== Admin: Getting All Videos - Page: {}, Size: {}, Search: {}, Premium: {}, Sort: {} {} ===",
                page, size, search, isPremium, sortBy, sortDirection);

        PagedResponse<VideoResponse> videos = videoService.getAllVideosForAdmin(
                page, size, search, isPremium, sortBy, sortDirection);

        log.info("=== All Videos Retrieved Successfully ===");
        return ApiResponse.<PagedResponse<VideoResponse>>builder()
                .result(videos)
                .message("All videos retrieved successfully")
                .build();
    }


    @GetMapping("/monthly/{profileId}")
    @Operation(
            summary = "Get monthly video statistics",
            description = "Get video uploads and engagement statistics grouped by month"
    )
    public ApiResponse<VideoMonthlyStatsResponse> getMonthlyStats(
            @PathVariable String profileId,
            @RequestParam(required = false, defaultValue = "6") Integer months) {

        log.info("=== Getting Monthly Stats for Profile: {} (Last {} months) ===", profileId, months);

        VideoMonthlyStatsResponse response = videoService.getMonthlyStatsByProfileId(profileId, months);

        return ApiResponse.<VideoMonthlyStatsResponse>builder()
                .message("Monthly video statistics retrieved successfully")
                .result(response)
                .build();
    }

    @GetMapping("/admin/growth-data")
    public ApiResponse<GrowthDataResponse> getGrowthData(
            @RequestParam(defaultValue = "week") String timeRange,
            @RequestParam(defaultValue = "previous") String comparisonType,
            @RequestParam(required = false) String customStartDate,
            @RequestParam(required = false) String customEndDate
    ) {
        log.info("=== Admin: Getting Growth Data - TimeRange: {}, Comparison: {} ===",
                timeRange, comparisonType);

        GrowthDataResponse growthData = videoService.getGrowthData(
                timeRange, comparisonType, customStartDate, customEndDate);

        log.info("=== Growth Data Retrieved Successfully ===");
        return ApiResponse.<GrowthDataResponse>builder()
                .result(growthData)
                .message("Growth data retrieved successfully")
                .build();
    }

    @GetMapping("/count/{profileId}")
    public ApiResponse<Long> getVideoCountByProfile(@PathVariable String profileId) {
        log.info("Getting video count for profile: {}", profileId);

        Long count = videoRepository.countByProfileId(profileId);

        return ApiResponse.<Long>builder()
                .result(count)
                .message("Video count retrieved successfully")
                .build();
    }


    /**
     * Record valid view - gọi khi đạt ngưỡng xem hợp lệ
     */
    @PostMapping("/{videoId}/valid-view")
    public ApiResponse<Void> recordValidView(
            @PathVariable String videoId,
            @RequestBody ViewTrackingRequest request,
            HttpServletRequest httpRequest) {

        log.info("=== Received Valid View Request for Video: {} ===", videoId);

        // Optional: Capture IP and User-Agent từ request
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        request.setIpAddress(ipAddress);
        request.setUserAgent(userAgent);

        videoService.recordValidView(videoId, request);

        return ApiResponse.<Void>builder()
                .message("Valid view recorded")
                .build();
    }

    /**
     * Update view progress - gọi định kỳ trong quá trình xem
     */
    @PostMapping("/{videoId}/view-progress")
    public ApiResponse<Void> updateViewProgress(
            @PathVariable String videoId,
            @RequestBody ViewProgressRequest request) {

        log.info("=== Received View Progress Update for Video: {} ===", videoId);

        videoService.updateViewProgress(videoId, request);

        return ApiResponse.<Void>builder()
                .message("View progress updated")
                .build();
    }

    /**
     * Mark interaction (like, comment, share) - tự động count valid view
     */
    @PostMapping("/{videoId}/mark-interaction")
    public ApiResponse<Void> markInteraction(
            @PathVariable String videoId,
            @RequestParam String sessionId) {

        log.info("=== Received Interaction Mark for Video: {} ===", videoId);

        videoService.markInteraction(videoId, sessionId);

        return ApiResponse.<Void>builder()
                .message("Interaction marked")
                .build();
    }

    /**
     * Get detailed view statistics
     */
    @GetMapping("/{videoId}/view-stats")
    public ApiResponse<ViewStatsResponse> getViewStats(@PathVariable String videoId) {
        log.info("=== Getting View Stats for Video: {} ===", videoId);

        ViewStatsResponse stats = videoService.getViewStats(videoId);

        return ApiResponse.<ViewStatsResponse>builder()
                .result(stats)
                .message("View stats retrieved successfully")
                .build();
    }

    /**
     * Helper method to get client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Get first IP if multiple
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }
}

