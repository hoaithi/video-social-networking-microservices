package com.hoaithi.video_service.service;

import com.hoaithi.video_service.dto.request.VideoCreationRequest;
import com.hoaithi.video_service.dto.response.VideoResponse;
import com.hoaithi.video_service.entity.Video;
import com.hoaithi.video_service.exception.AppException;
import com.hoaithi.video_service.exception.ErrorCode;
import com.hoaithi.video_service.mapper.VideoMapper;
import com.hoaithi.video_service.repository.httpclient.FileClient;
import com.hoaithi.video_service.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class VideoService {
    VideoRepository videoRepository;
    VideoMapper videoMapper;
    FileClient fileClient;

    public List<Video> getAllVideos() {
        return videoRepository.findAll();
    }

    public VideoResponse getVideoById(String id) {
        Video video = videoRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
        return videoMapper.toVideoResponse(video);
    }

    public Video updateVideo(String id, Video newVideo) {
        return videoRepository.findById(id).map(video -> {
            video.setTitle(newVideo.getTitle());
            video.setDescription(newVideo.getDescription());
            video.setDuration(newVideo.getDuration());
            video.setThumbnailUrl(newVideo.getThumbnailUrl());
            video.setVideoUrl(newVideo.getVideoUrl());
            return videoRepository.save(video);
        }).orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
    }

    public void deleteVideo(String id) {
        videoRepository.deleteById(id);
    }

    public VideoResponse createVideo(MultipartFile videoFile, MultipartFile thumbnailFile, VideoCreationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Video video = Video.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .isPremium(request.isPremium())
                .build();
        video.setVideoUrl(fileClient.uploadFile(videoFile));
        video.setThumbnailUrl(fileClient.uploadFile(thumbnailFile));
        video.setPublishedAt(LocalDateTime.now());
        video.setUserId(authentication.getName());
        double durationInSeconds = 0.0;
        try {
            // convert MultipartFile -> File tạm
            File convFile = File.createTempFile("upload", ".mp4");
            videoFile.transferTo(convFile);

            durationInSeconds = getVideoDuration(convFile);

            convFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        video.setDuration(Math.round(durationInSeconds));
        return videoMapper.toVideoResponse(videoRepository.save(video));
    }


    private static double getVideoDuration(File file) throws Exception {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file)) {
            grabber.start();
            double durationInSeconds = (double) grabber.getLengthInTime() / 1000000.0; // microseconds -> seconds
            grabber.stop();
            return durationInSeconds;
        }
    }

}

