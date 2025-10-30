package com.hoaithi.video_service.controller;
import com.hoaithi.video_service.dto.request.VideoProgressUpdateRequest;
import com.hoaithi.video_service.dto.response.ApiResponse;
import com.hoaithi.video_service.dto.response.PagedResponse;
import com.hoaithi.video_service.dto.response.VideoResponse;
import com.hoaithi.video_service.service.HistoryService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/playlist/history")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class HistoryController {
    HistoryService historyService;


    @PostMapping("/{videoId}")
    public ApiResponse<?> createHistory(@PathVariable("videoId") String videoId, @RequestBody VideoProgressUpdateRequest request){
        log.info("history "+videoId);
        log.info("history "+ request.getCurrentTime());
        return ApiResponse.builder().build();
    }


    @GetMapping()
    public ApiResponse<?> getVideoWatchLaters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ApiResponse.<PagedResponse<VideoResponse>>builder()
                .result(historyService.getVideoHistories(page, size))
                .build();
    }
    @DeleteMapping("/{videoId}")
    public ApiResponse<?> delete(@PathVariable("videoId") String videoId){
        historyService.delete(videoId);
        return ApiResponse.<Void>builder()
                .message("Delete video history successfully")
                .build();
    }
    @DeleteMapping("/clear")
    public ApiResponse<?> clear(){
        historyService.clear();
        return ApiResponse.<Void>builder()
                .message("Delete video history successfully")
                .build();
    }

}
