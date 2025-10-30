package com.hoaithi.video_service.service;

import com.hoaithi.video_service.dto.request.VideoProgressUpdateRequest;
import com.hoaithi.video_service.dto.response.PagedResponse;
import com.hoaithi.video_service.dto.response.ProfileResponse;
import com.hoaithi.video_service.dto.response.VideoResponse;
import com.hoaithi.video_service.entity.Video;
import com.hoaithi.video_service.entity.VideoHistory;
import com.hoaithi.video_service.exception.AppException;
import com.hoaithi.video_service.exception.ErrorCode;
import com.hoaithi.video_service.mapper.VideoMapper;
import com.hoaithi.video_service.mapper.VideoMapperImpl;
import com.hoaithi.video_service.repository.HistoryRepository;
import com.hoaithi.video_service.repository.VideoRepository;
import com.hoaithi.video_service.repository.httpclient.ProfileClient;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class HistoryService {
    HistoryRepository historyRepository;
    VideoMapper videoMapper;
    VideoRepository videoRepository;
    ProfileClient profileClient;

    public PagedResponse<VideoResponse> getVideoHistories(int page, int size) {
        Pageable pageable= PageRequest.of(page,size, Sort.by("createdAt").descending());
        Page<Video> videos = historyRepository.findAllVideosByProfileId(getCurrentUserId(), pageable);
        Page<VideoResponse> responses = videos.map(video -> {
            VideoResponse videoResponse = videoMapper.toVideoResponse(video);
            videoResponse.setPremium(video.isPremium());
            ProfileResponse profileResponse = profileClient.getProfileById(videoResponse.getProfileId()).getResult();
            videoResponse.setProfileImage(profileResponse.getAvatarUrl());
            videoResponse.setProfileName(profileResponse.getFullName());
            return videoResponse;
        });
        return PagedResponse.<VideoResponse>builder()
                .content(responses.getContent())
                .page(responses.getNumber())
                .totalPages(responses.getTotalPages())
                .totalElements(responses.getTotalElements())
                .size(responses.getSize())
                .last(responses.isLast())
                .build();
    }



    public void updateVideoProgress(String videoId, VideoProgressUpdateRequest request) {
        String profileId = getCurrentUserId();
        Video video = videoRepository.findById(videoId).orElseThrow(()-> new AppException(ErrorCode.VIDEO_NOT_EXISTED)) ;

        // Find existing progress or create new one
        VideoHistory history = historyRepository.findByProfileIdAndVideoId(profileId, videoId)
                .orElse(VideoHistory.builder()
                        .video(video)
                        .profileId(profileId)
                        .build());

        // Update fields
        history.setCurrentTime(request.getCurrentTime());
        history.setDuration(request.getDuration());
        history.setLastWatched(LocalDateTime.now());

        // Calculate percentage
        double percentage = (double) request.getCurrentTime() / request.getDuration() * 100;
        history.setPercentage(Math.min(percentage, 100.0));

        // Mark as completed if watched > 90%
        boolean isCompleted = request.getIsCompleted() != null ?
                request.getIsCompleted() : percentage >= 90.0;
        history.setCompleted(isCompleted);

        historyRepository.save(history);
    }
    @Transactional
    public void delete(String videoId) {
        if(historyRepository.existsByVideoIdAndProfileId(videoId, getCurrentUserId())){
            historyRepository.deleteByVideoIdAndProfileId(videoId, getCurrentUserId());
        }
    }

    private String getCurrentUserId(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Transactional
    public void clear() {
        historyRepository.deleteAllByProfileId(getCurrentUserId());
    }
}
