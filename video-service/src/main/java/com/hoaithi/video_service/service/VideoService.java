package com.hoaithi.video_service.service;

import com.hoaithi.video_service.dto.request.VideoCreationRequest;
import com.hoaithi.video_service.dto.response.VideoResponse;
import com.hoaithi.video_service.entity.Video;
import com.hoaithi.video_service.mapper.VideoMapper;
import com.hoaithi.video_service.repository.FileClient;
import com.hoaithi.video_service.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    public Optional<Video> getVideoById(String id) {
        return videoRepository.findById(id);
    }

    public Video updateVideo(String id, Video newVideo) {
        return videoRepository.findById(id).map(video -> {
            video.setTitle(newVideo.getTitle());
            video.setDescription(newVideo.getDescription());
            video.setDuration(newVideo.getDuration());
            video.setThumbnailUrl(newVideo.getThumbnailUrl());
            video.setVideoUrl(newVideo.getVideoUrl());
            return videoRepository.save(video);
        }).orElseThrow(() -> new RuntimeException("Video not found"));
    }

    public void deleteVideo(String id) {
        videoRepository.deleteById(id);
    }

    public VideoResponse createVideo(MultipartFile videoFile, MultipartFile thumbnailFile, VideoCreationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Video video = videoMapper.toVideo(request);
        video.setVideoUrl(fileClient.uploadFile(videoFile));
        video.setThumbnailUrl(fileClient.uploadFile(thumbnailFile));
        video.setPublishedAt(LocalDateTime.now());
        video.setUserId(authentication.getName());
        return videoMapper.toVideoResponse(videoRepository.save(video));
    }
}

