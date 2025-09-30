package com.hoaithi.video_service.controller;

import com.hoaithi.video_service.dto.request.VideoCreationRequest;
import com.hoaithi.video_service.dto.response.ApiResponse;
import com.hoaithi.video_service.dto.response.PagedResponse;
import com.hoaithi.video_service.dto.response.VideoResponse;
import com.hoaithi.video_service.entity.Video;
import com.hoaithi.video_service.service.HeartService;
import com.hoaithi.video_service.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/video")
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class VideoController {

    VideoService videoService;
    HeartService heartService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<VideoResponse> createVideo(
            @RequestPart(value = "videoFile", required = false) MultipartFile videoFile,
            @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            @RequestPart(value = "video", required = false) VideoCreationRequest request

    ) {
        VideoResponse response = videoService.createVideo(videoFile, thumbnailFile, request);

        return ApiResponse.<VideoResponse>builder()
                .result(response)
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<PagedResponse<VideoResponse>> getVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ){

        return ApiResponse.<PagedResponse<VideoResponse>>builder()
                .result(videoService.getVideos(page, size))
                .message("Retrieve list of video successfully")
                .build();
    }

    @GetMapping("/watch/{id}")
    public ApiResponse<VideoResponse> getVideoById(@PathVariable("id") String id) {
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
    ){

        return ApiResponse.<PagedResponse<VideoResponse>>builder()
                .result(videoService.getVideoByProfileId(profileId, page,size))
                .message("Retrieve list of video successfully")
                .build();
    }

    @PostMapping("/{id}/heart")
    public ApiResponse<?> tymVideo(@PathVariable("id") String id){
        return ApiResponse.<Object>builder()
                .result( heartService.heartVideo(id))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteVideoById(@PathVariable("id") String id){
        log.info("delete video api");
        videoService.deleteVideo(id);
        return ApiResponse.<Void>builder().build();
    }


}
