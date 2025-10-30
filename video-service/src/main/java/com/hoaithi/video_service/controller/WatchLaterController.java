package com.hoaithi.video_service.controller;
import com.hoaithi.video_service.dto.request.WatchLaterRequest;
import com.hoaithi.video_service.dto.response.ApiResponse;
import com.hoaithi.video_service.dto.response.PagedResponse;
import com.hoaithi.video_service.dto.response.VideoResponse;
import com.hoaithi.video_service.service.WatchLaterService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/playlist/watch-later")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class WatchLaterController {
    WatchLaterService watchLaterService;

    @PostMapping
    public ApiResponse<Void> create(@RequestBody WatchLaterRequest request) {
        watchLaterService.create(request);
        return ApiResponse.<Void>builder()
                .message("create watch later successfully")
                .build();
    }

    @GetMapping()
    public ApiResponse<?> getVideoWatchLaters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ApiResponse.<PagedResponse<VideoResponse>>builder()
                .result(watchLaterService.getVideoWatchLaters(page, size))
                .build();
    }
    @DeleteMapping("/{videoId}")
    public ApiResponse<?> delete(@PathVariable("videoId") String videoId){
            watchLaterService.delete(videoId);
        return ApiResponse.<Void>builder()
                .message("Delete video watch later successfully")
                .build();
    }
}

