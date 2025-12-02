package com.hoaithi.ai_service.controller;

import com.hoaithi.ai_service.dto.response.DescriptionResponse;
import com.hoaithi.ai_service.dto.response.TitleResponse;
import com.hoaithi.ai_service.service.OptimizedChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v2")
public class OptimizedChatController {

    private final OptimizedChatService chatService;

    public OptimizedChatController(OptimizedChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/titles")
    public ResponseEntity<List<TitleResponse>> generateTitles(
            @RequestParam("video") MultipartFile video) {
        try {
            List<TitleResponse> titles = chatService.generateTitle(video);
            return ResponseEntity.ok(titles);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/description")
    public ResponseEntity<DescriptionResponse> generateDescription(
            @RequestParam("video") MultipartFile video) {
        try {
            DescriptionResponse description = chatService.generateDescription(video);
            return ResponseEntity.ok(description);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}