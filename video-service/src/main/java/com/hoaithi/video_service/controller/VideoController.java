package com.hoaithi.video_service.controller;

import com.hoaithi.video_service.dto.request.VideoCreationRequest;
import com.hoaithi.video_service.dto.response.ApiResponse;
import com.hoaithi.video_service.dto.response.VideoResponse;
import com.hoaithi.video_service.service.HeartService;
import com.hoaithi.video_service.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@RequestMapping("/video")
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

    @GetMapping()
    public ApiResponse<List<VideoResponse>> getVideos(){

        return ApiResponse.<List<VideoResponse>>builder()
                .result(videoService.getVideos())
                .message("Retrieve list of video successfully")
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<VideoResponse> getVideoById(@PathVariable("id") String id) {
        VideoResponse response = videoService.getVideoById(id);
        return ApiResponse.<VideoResponse>builder()
                .result(response)
                .build();
    }

    @PostMapping("/{id}/tym")
    public ApiResponse<?> tymVideo(@PathVariable("id") String id){
        return ApiResponse.<Object>builder()
                .result( heartService.tymVideo(id))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteVideoById(@PathVariable("id") String id){
        log.info("delete video api");
        videoService.deleteVideo(id);
        return ApiResponse.<Void>builder().build();
    }


}
