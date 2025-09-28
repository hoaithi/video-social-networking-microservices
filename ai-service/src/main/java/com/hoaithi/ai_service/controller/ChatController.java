package com.hoaithi.ai_service.controller;

import com.hoaithi.ai_service.dto.ChatRequest;
import com.hoaithi.ai_service.dto.TranscriptionRequest;
import com.hoaithi.ai_service.dto.TranscriptionResponse;
import com.hoaithi.ai_service.service.ChatService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ChatController {
    private final ChatService chatService;
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }
    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request) {
        return chatService.chat(request);
    }
    @PostMapping("/transcribe")
    public TranscriptionResponse transcribe(
            @RequestPart("video")MultipartFile video,
            @RequestPart("message") String message) throws Exception {
        return chatService.transcribeToString(video, message);
    }
}
