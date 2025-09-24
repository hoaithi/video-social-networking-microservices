package com.hoaithi.ai_service.dto;

import java.util.List;

public record TranscriptionResponse(List<String> titles, String language) {
}
