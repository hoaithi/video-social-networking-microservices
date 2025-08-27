package com.hoaithi.video_service.controller;

import com.hoaithi.video_service.dto.request.VideoCreationRequest;
import com.hoaithi.video_service.dto.response.ApiResponse;
import com.hoaithi.video_service.dto.response.VideoResponse;
import com.hoaithi.video_service.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@RequestMapping("/videos")
public class VideoController {

    VideoService videoService;

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

}
