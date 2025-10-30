package com.hoaithi.ai_service.service;

import com.hoaithi.ai_service.dto.request.ChatRequest;
import com.hoaithi.ai_service.dto.response.DescriptionResponse;
import com.hoaithi.ai_service.dto.response.TitleResponse;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClient) {
        this.chatClient = chatClient.build();
    }
    public String chat(ChatRequest request) {
        Prompt prompt = new Prompt(request.message());
        return chatClient
                .prompt(prompt)
                .call()
                .content();
    }
    public TitleResponse transcribe(MultipartFile video, String message){
        Media media = new Media(MimeType.valueOf("video/mp4"), video.getResource());
        return chatClient.prompt()
                .user(promptUserSpec -> promptUserSpec
                        .text(message)
                        .media(media)
                )
                .call()
                .entity(TitleResponse.class);
    }


    public List<TitleResponse> generateTilte(MultipartFile video) throws Exception {
        File audio = null;

        try {
            audio = extractAudio(video);
            Media audioMedia = new Media(MimeType.valueOf("audio/mp3"), new FileSystemResource(audio));
            return chatClient.prompt()
                    .system(systemSpec -> systemSpec
                            .text("You are an AI that listens to audio and generates meaningful English titles.")
                    )
                    .user(promptUserSpec -> promptUserSpec
                            .text("create 5 title from content of the video")
                            .media(audioMedia)
                    )
                    .call()
                    .entity(new ParameterizedTypeReference<>() {
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if(audio != null && audio.exists()){
                boolean deleted = audio.delete();
                if(!deleted){
                    log.info("Warning: could not delete temp file: " + audio.getAbsolutePath());
                }
            }
        }

    }

    private File extractAudio(MultipartFile videoFile) throws Exception {
        File tempInput = File.createTempFile("video-", videoFile.getOriginalFilename());
        videoFile.transferTo(tempInput);

        File tempOutput = File.createTempFile("audio-", ".mp3");

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(tempInput);
             FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(tempOutput, 0)) {

            grabber.start();

            recorder.setAudioCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_MP3);
            recorder.setFormat("mp3");
            recorder.setSampleRate(grabber.getSampleRate());
            recorder.setAudioBitrate(128 * 1000);
            recorder.setAudioChannels(grabber.getAudioChannels());

            recorder.start();

            while (true) {
                var frame = grabber.grabSamples();
                if (frame == null) break;
                recorder.record(frame);
            }

            recorder.stop();
            grabber.stop();
        } finally {
            // Dọn file video tạm
            if (tempInput.exists()) {
                tempInput.delete();
            }
        }

        return tempOutput;
    }

    public DescriptionResponse generateDescription(MultipartFile video) {
        File audio = null;

        try {
            audio = extractAudio(video);
            Media audioMedia = new Media(MimeType.valueOf("audio/mp3"), new FileSystemResource(audio));
            return chatClient.prompt()
                    .system(systemSpec -> systemSpec
                            .text("You are an AI that listens to audio and generates meaningful English description.")
                    )
                    .user(promptUserSpec -> promptUserSpec
                            .text("create description from content of the video")
                            .media(audioMedia)
                    )
                    .call()
                    .entity(new ParameterizedTypeReference<>() {
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if(audio != null && audio.exists()){
                boolean deleted = audio.delete();
                if(!deleted){
                    log.info("Warning: could not delete temp file: " + audio.getAbsolutePath());
                }
            }
        }
    }
}
