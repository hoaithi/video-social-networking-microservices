package com.hoaithi.ai_service.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record TranscriptionRequest(MultipartFile audioFile, String message) {
}
