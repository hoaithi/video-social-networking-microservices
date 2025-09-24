package com.hoaithi.ai_service.dto;

import org.springframework.web.multipart.MultipartFile;

public record TranscriptionRequest(MultipartFile audioFile, String message) {
}
