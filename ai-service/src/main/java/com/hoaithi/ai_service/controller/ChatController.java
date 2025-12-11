package com.hoaithi.ai_service.controller;

import com.hoaithi.ai_service.dto.request.ChatRequest;
import com.hoaithi.ai_service.dto.response.ApiResponse;
import com.hoaithi.ai_service.dto.response.DescriptionResponse;
import com.hoaithi.ai_service.dto.response.TitleResponse;
import com.hoaithi.ai_service.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@Slf4j
@RestController
public class ChatController {
    private final ChatService chatService;
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }
    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request) {
        log.info("=== START /chat endpoint ===");
        log.info("Request content: {}", request);
        String response = chatService.chat(request);
        log.info("Chat response generated successfully");
        log.debug("Response preview: {}", response.substring(0, Math.min(100, response.length())));
        log.info("=== END /chat endpoint ===");
        return response;
    }


    @PostMapping("/generate-title")
    public ApiResponse<List<TitleResponse>> generateTitle(
            @RequestPart("videoFile") MultipartFile video) throws Exception {
//        log.info("=== START /generate-title endpoint ===");
//        log.info("Received video file: name={}, size={} bytes, contentType={}",
//                video.getOriginalFilename(), video.getSize(), video.getContentType());
        return ApiResponse.<List<TitleResponse>>builder()
                .message("")
                .result(chatService.generateTilte(video))
                .build();
    }

    @PostMapping("/generate-description")
    public ApiResponse<DescriptionResponse> generateDescription(
            @RequestPart("videoFile") MultipartFile video) throws Exception {

        return ApiResponse.<DescriptionResponse>builder()
                .message("")
                .result(chatService.generateDescription(video))
                .build();
    }
}
