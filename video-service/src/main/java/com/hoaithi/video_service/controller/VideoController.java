package com.hoaithi.video_service.controller;

import com.hoaithi.video_service.dto.request.VideoCreationRequest;
import com.hoaithi.video_service.dto.response.ApiResponse;
import com.hoaithi.video_service.dto.response.PagedResponse;
import com.hoaithi.video_service.dto.response.VideoResponse;
import com.hoaithi.video_service.dto.response.VideoUserReaction;
import com.hoaithi.video_service.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/video")
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class VideoController {

    VideoService videoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<VideoResponse> createVideo(
            @RequestPart(value = "videoFile", required = false) MultipartFile videoFile,
            @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            @RequestPart(value = "title", required = false) String title,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "isPremium", required = false) String isPremium
    ) {
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
    public ApiResponse<PagedResponse<VideoResponse>> getVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {

        return ApiResponse.<PagedResponse<VideoResponse>>builder()
                .result(videoService.getVideos(page, size))
                .message("Retrieve list of video successfully")
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
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<VideoResponse> likeVideo(
            @PathVariable("videoId") String videoId) {

        VideoResponse response = videoService.likeVideo(videoId);

        return ApiResponse.<VideoResponse>builder()
                .result(response)
                .build();
    }

    @PostMapping("/{videoId}/dislike")
    @PreAuthorize("hasRole('USER')")
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
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<VideoUserReaction>> getVideoUserReaction(
            @PathVariable String videoId
    ){
        VideoUserReaction videoUserReaction = videoService.getUserReaction(videoId);
        return ResponseEntity.ok(ApiResponse.<VideoUserReaction>builder()
                .message("Video user reaction retrieved successfully")
                .result(videoUserReaction)
                .build());
    }


}
