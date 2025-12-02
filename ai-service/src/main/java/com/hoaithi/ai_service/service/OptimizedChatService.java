package com.hoaithi.ai_service.service;

import com.hoaithi.ai_service.dto.response.DescriptionResponse;
import com.hoaithi.ai_service.dto.response.TitleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class OptimizedChatService {

    private static final Logger log = LoggerFactory.getLogger(OptimizedChatService.class);
    private final ChatClient.Builder chatClientBuilder;

    // Constants - GIẢM XUỐNG để chắc chắn
    private static final int SHORT_SEGMENT_DURATION = 60; // Giảm xuống 60s
    private static final int MAX_TRANSCRIPTION_FOR_PROCESSING = 1500; // Max chars trước khi summarize
    private static final int TARGET_SUMMARY_LENGTH = 400; // Target summary length

    public OptimizedChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    /**
     * MAIN METHOD: Generate titles
     * Strategy: Audio -> Transcription -> Compact Summary -> Titles
     */
    public List<TitleResponse> generateTitle(MultipartFile video) throws Exception {
        File audioSegment = null;

        try {
            log.info("=== Title Generation Started ===");
            log.info("Video size: {:.2f} MB", video.getSize() / (1024.0 * 1024.0));

            // Step 1: Extract SHORT audio (60s only)
            audioSegment = extractAudioUsingFFmpeg(video, SHORT_SEGMENT_DURATION);

            if (audioSegment == null || !audioSegment.exists()) {
                throw new RuntimeException("Failed to extract audio segment");
            }

            log.info("Audio extracted: {:.2f} MB", audioSegment.length() / (1024.0 * 1024.0));

            // Step 2: Transcribe ONLY (no processing in same call)
            String rawTranscription = transcribeAudioSafe(audioSegment);
            log.info("Transcription: {} chars", rawTranscription.length());

            // Step 3: Create compact summary in SEPARATE call
            String compactSummary = createCompactSummary(rawTranscription);
            log.info("Compact summary: {} chars", compactSummary.length());

            // Step 4: Generate titles from compact summary
            List<TitleResponse> titles = generateTitlesFromSummary(compactSummary);

            log.info("=== {} titles generated ===", titles.size());
            return titles;

        } catch (Exception e) {
            log.error("Error in generateTitle", e);
            throw new RuntimeException("Title generation failed: " + e.getMessage(), e);
        } finally {
            cleanupFile(audioSegment, "audio segment");
        }
    }

    /**
     * Extract audio using FFmpeg command
     * CRITICAL: Only extract N seconds
     */
    private File extractAudioUsingFFmpeg(MultipartFile videoFile, int durationSeconds) {
        File tempInput = null;
        File audioOutput = null;

        try {
            log.info("Extracting {}s audio using FFmpeg...", durationSeconds);

            // Save video to temp
            tempInput = File.createTempFile("video-", ".mp4");
            videoFile.transferTo(tempInput);

            // Output audio
            audioOutput = File.createTempFile("audio-", ".mp3");

            // FFmpeg command
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", tempInput.getAbsolutePath(),
                    "-t", String.valueOf(durationSeconds),  // LIMIT DURATION
                    "-vn",                                   // No video
                    "-acodec", "libmp3lame",
                    "-ac", "1",                              // Mono
                    "-ar", "22050",                          // Lower sample rate
                    "-b:a", "64k",                           // Lower bitrate
                    "-y",
                    audioOutput.getAbsolutePath()
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Capture output
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("time=")) {
                        log.debug("FFmpeg: {}", line);
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("FFmpeg failed with exit code: {}", exitCode);
                return null;
            }

            log.info("Audio extracted successfully: {:.2f} MB",
                    audioOutput.length() / (1024.0 * 1024.0));

            return audioOutput;

        } catch (Exception e) {
            log.error("FFmpeg extraction failed", e);
            cleanupFile(audioOutput, "failed audio output");
            return null;
        } finally {
            cleanupFile(tempInput, "temp video input");
        }
    }

    /**
     * Transcribe audio - SAFE version
     * Returns transcription in separate call to avoid context buildup
     */
    private String transcribeAudioSafe(File audioFile) {
        try {
            log.info("Transcribing audio...");

            Media audioMedia = new Media(
                    MimeType.valueOf("audio/mp3"),
                    new FileSystemResource(audioFile)
            );

            // Create ISOLATED chat client for transcription only
            ChatClient transcriptionClient = chatClientBuilder.build();

            String transcription = transcriptionClient
                    .prompt()
                    .system("You are a transcription AI. Transcribe speech accurately and concisely. " +
                            "Focus on main topics and key points only.")
                    .user(spec -> spec
                            .text("Transcribe this audio. Extract main topics and key information only.")
                            .media(audioMedia)
                    )
                    .call()
                    .content();

            return transcription != null ? transcription.trim() : "";

        } catch (Exception e) {
            log.error("Transcription failed", e);
            return "Transcription unavailable";
        }
    }

    /**
     * Create compact summary - SEPARATE call
     * This is the KEY to avoiding Jackson overflow
     */
    private String createCompactSummary(String transcription) {
        try {
            // If already short, use directly
            if (transcription.length() <= TARGET_SUMMARY_LENGTH) {
                log.info("Transcription short enough, using directly");
                return transcription;
            }

            // Truncate first to avoid sending too much
            String toSummarize = transcription.length() > MAX_TRANSCRIPTION_FOR_PROCESSING
                    ? transcription.substring(0, MAX_TRANSCRIPTION_FOR_PROCESSING)
                    : transcription;

            log.info("Creating summary from {} chars...", toSummarize.length());

            // Create NEW isolated client for summary
            ChatClient summaryClient = chatClientBuilder.build();

            String summary = summaryClient
                    .prompt()
                    .system("You are a summarization expert. Extract the core topic and main points " +
                            "in under 80 words.")
                    .user("Summarize the main topic and key points:\n\n" + toSummarize)
                    .call()
                    .content();

            // Final safety check
            if (summary.length() > TARGET_SUMMARY_LENGTH) {
                log.warn("Summary too long ({}), truncating", summary.length());
                return summary.substring(0, TARGET_SUMMARY_LENGTH);
            }

            return summary;

        } catch (Exception e) {
            log.error("Summary creation failed", e);
            // Fallback: use truncated transcription
            return transcription.substring(0, Math.min(TARGET_SUMMARY_LENGTH, transcription.length()));
        }
    }

    /**
     * Generate titles from compact summary
     * Summary is SHORT -> no Jackson issues
     */
    private List<TitleResponse> generateTitlesFromSummary(String summary) {
        try {
            log.info("Generating titles from summary ({} chars)...", summary.length());

            // Create NEW isolated client for titles
            ChatClient titleClient = chatClientBuilder.build();

            List<TitleResponse> titles = titleClient
                    .prompt()
                    .system("You are a creative title generator. Create engaging, descriptive titles.")
                    .user("Generate 5 catchy, meaningful titles based on:\n\n" + summary)
                    .call()
                    .entity(new ParameterizedTypeReference<List<TitleResponse>>() {});

            return titles != null ? titles : new ArrayList<>();

        } catch (Exception e) {
            log.error("Title generation failed", e);

            // Fallback titles
            List<TitleResponse> fallback = new ArrayList<>();
//            fallback.add(new TitleResponse("Video Title"));
            return fallback;
        }
    }

    /**
     * Generate description
     */
    public DescriptionResponse generateDescription(MultipartFile video) throws Exception {
        File audioSegment = null;

        try {
            log.info("=== Description Generation Started ===");

            // Extract longer segment for description (120s)
            audioSegment = extractAudioUsingFFmpeg(video, 120);

            if (audioSegment == null) {
                throw new RuntimeException("Failed to extract audio");
            }

            // Transcribe
            String transcription = transcribeAudioSafe(audioSegment);
            log.info("Transcription: {} chars", transcription.length());

            // Process to reasonable length
            String processed = transcription.length() > 2000
                    ? transcription.substring(0, 2000)
                    : transcription;

            // Generate description in isolated call
            ChatClient descClient = chatClientBuilder.build();

            DescriptionResponse description = descClient
                    .prompt()
                    .system("You are a content summarizer. Create engaging descriptions.")
                    .user("Create a detailed description based on:\n\n" + processed)
                    .call()
                    .entity(DescriptionResponse.class);

            log.info("=== Description generated ===");
            return description;

        } catch (Exception e) {
            log.error("Description generation failed", e);
            throw new RuntimeException("Failed to generate description", e);
        } finally {
            cleanupFile(audioSegment, "audio segment");
        }
    }

    private void cleanupFile(File file, String description) {
        if (file != null && file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                log.warn("Could not delete {}: {}", description, file.getAbsolutePath());
            }
        }
    }
}