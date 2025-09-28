package com.hoaithi.video_service.controller;

import com.hoaithi.video_service.dto.response.ApiResponse;
import com.hoaithi.video_service.dto.response.VideoResponse;
import com.hoaithi.video_service.service.HistoryService;
import com.hoaithi.video_service.service.HeartService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/playlist")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class PlaylistController {
    HistoryService historyService;
    HeartService heartService;


    @GetMapping()
    public ApiResponse<?> getPlaylists(){
        return ApiResponse.<Object>builder().build();
    }

    @GetMapping("/history")
    public ApiResponse<List<VideoResponse>> getHistory(){
        return ApiResponse.<List<VideoResponse>>builder()
                .result(historyService.getVideoHistories())
                .build();
    }
    @GetMapping("/tym")
    public ApiResponse<?> getTym(){
        return ApiResponse.<List<VideoResponse>>builder()
                .result(heartService.getVideoTyms())
                .build();
    }
}
