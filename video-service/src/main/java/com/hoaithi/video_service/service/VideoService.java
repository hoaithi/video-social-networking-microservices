package com.hoaithi.video_service.service;

import com.hoaithi.video_service.entity.Video;
import com.hoaithi.video_service.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final VideoRepository videoRepository;

    public Video createVideo(Video video) {
        video.setPublishedAt(LocalDateTime.now());
        return videoRepository.save(video);
    }

    public List<Video> getAllVideos() {
        return videoRepository.findAll();
    }

    public Optional<Video> getVideoById(Long id) {
        return videoRepository.findById(id);
    }

    public Video updateVideo(Long id, Video newVideo) {
        return videoRepository.findById(id).map(video -> {
            video.setTitle(newVideo.getTitle());
            video.setDescription(newVideo.getDescription());
            video.setDuration(newVideo.getDuration());
            video.setThumbnailUrl(newVideo.getThumbnailUrl());
            video.setVideoUrl(newVideo.getVideoUrl());
            video.setIsPremium(newVideo.getIsPremium());
            return videoRepository.save(video);
        }).orElseThrow(() -> new RuntimeException("Video not found"));
    }

    public void deleteVideo(Long id) {
        videoRepository.deleteById(id);
    }
}

