package com.hoaithi.video_service.controller;

import com.hoaithi.video_service.dto.response.OwnerIdResponse;
import com.hoaithi.video_service.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class InternalVideoController {
    VideoService videoService;
    @GetMapping("/internal/video/{videoId}")
    public OwnerIdResponse getOwnerId(@PathVariable String videoId){
        return videoService.getOwnerId(videoId);
    }
}
