package com.hoaithi.video_service.controller;

import com.hoaithi.video_service.dto.request.PlaylistCreationRequest;
import com.hoaithi.video_service.dto.response.ApiResponse;
import com.hoaithi.video_service.dto.response.PlaylistResponse;
import com.hoaithi.video_service.dto.response.VideoResponse;
import com.hoaithi.video_service.service.HistoryService;
import com.hoaithi.video_service.service.HeartService;
import com.hoaithi.video_service.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/playlist")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class PlaylistController {
    HistoryService historyService;
    HeartService heartService;
    PlaylistService playlistService;


    @GetMapping("/{profileId}")
    public ApiResponse<List<PlaylistResponse>> getPlaylists(@PathVariable String profileId){
        return ApiResponse.<List<PlaylistResponse>>builder()
                .result(playlistService.getPlaylists(profileId))
                .build();
    }

    @GetMapping("/my-playlist")
    public ApiResponse<List<PlaylistResponse>> getMyPlaylist(){
        return ApiResponse.<List<PlaylistResponse>>builder()
                .result(playlistService.getMyPlaylists())
                .build();
    }

    @PostMapping
    public ApiResponse<PlaylistResponse> createPlaylist(@RequestBody PlaylistCreationRequest request){
        return ApiResponse.<PlaylistResponse>builder()
                .result(playlistService.createPlaylist(request))
                .build();
    }

    @GetMapping("/history")
    public ApiResponse<List<VideoResponse>> getHistories(){
        return ApiResponse.<List<VideoResponse>>builder()
                .result(historyService.getVideoHistories())
                .build();
    }
    @GetMapping("/heart")
    public ApiResponse<?> getHearts(){
        return ApiResponse.<List<VideoResponse>>builder()
                .result(heartService.getVideoHearts())
                .build();
    }
}
