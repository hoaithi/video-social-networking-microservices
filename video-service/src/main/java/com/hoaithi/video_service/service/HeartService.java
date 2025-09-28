package com.hoaithi.video_service.service;

import com.hoaithi.video_service.dto.response.VideoHeartResponse;
import com.hoaithi.video_service.dto.response.VideoResponse;
import com.hoaithi.video_service.entity.Video;
import com.hoaithi.video_service.entity.VideoHeart;
import com.hoaithi.video_service.exception.AppException;
import com.hoaithi.video_service.exception.ErrorCode;
import com.hoaithi.video_service.mapper.VideoMapper;
import com.hoaithi.video_service.repository.VideoHeartRepository;
import com.hoaithi.video_service.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class HeartService {
    VideoHeartRepository videoHeartRepository;
    VideoMapper videoMapper;
    VideoRepository videoRepository;

    public List<VideoResponse> getVideoHearts(){
        String currentProfileId = getCurrentUserId();
        List<VideoHeart> videoHearts = videoHeartRepository.findByProfileId(currentProfileId);
        List<Video> videos = videoHearts.stream()
                .map(VideoHeart::getVideo)
                .toList();
        List<VideoResponse> videoResponses = new ArrayList<>();
        for (Video video: videos) {
            VideoResponse response = videoMapper.toVideoResponse(video);
            response.setPremium(video.isPremium());
            videoResponses.add(response);
        }
        return videoResponses;
    }
    public VideoHeartResponse heartVideo(String videoId){
        String currentProfileId = getCurrentUserId();
        boolean alreadyHeart = videoHeartRepository.existsByProfileIdAndVideoId(currentProfileId, videoId);
        if(!alreadyHeart){
            videoHeartRepository.save(VideoHeart.builder()
                    .video(videoRepository.findById(videoId).orElseThrow(()-> new AppException(ErrorCode.VIDEO_NOT_EXISTED)))
                    .profileId(currentProfileId)
                    .build());
        }else{
            videoHeartRepository.deleteByProfileIdAndVideoId(currentProfileId, videoId);
        }
        return VideoHeartResponse.builder()
                .videoId(videoId)
                .hearted(!alreadyHeart)
                .heartCount(videoHeartRepository.countByVideoId(videoId))
                .build();

    }
    private String getCurrentUserId(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
