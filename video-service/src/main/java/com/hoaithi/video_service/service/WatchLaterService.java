package com.hoaithi.video_service.service;

import com.hoaithi.video_service.dto.request.WatchLaterRequest;
import com.hoaithi.video_service.dto.response.PagedResponse;
import com.hoaithi.video_service.dto.response.ProfileResponse;
import com.hoaithi.video_service.dto.response.VideoResponse;
import com.hoaithi.video_service.entity.Video;
import com.hoaithi.video_service.entity.VideoWatchLater;
import com.hoaithi.video_service.exception.AppException;
import com.hoaithi.video_service.exception.ErrorCode;
import com.hoaithi.video_service.mapper.VideoMapper;
import com.hoaithi.video_service.repository.VideoRepository;
import com.hoaithi.video_service.repository.WatchLaterRepository;
import com.hoaithi.video_service.repository.httpclient.ProfileClient;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class WatchLaterService {
    WatchLaterRepository watchLaterRepository;
    VideoRepository videoRepository;
    VideoMapper videoMapper;
    ProfileClient profileClient;

    public void create(WatchLaterRequest request){
        if(!watchLaterRepository.existsByVideoIdAndProfileId(request.getVideoId(), getCurrentUserId())){
            watchLaterRepository.save(VideoWatchLater.builder()
                    .video(videoRepository.findById(request.getVideoId()).orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED)))
                    .profileId(getCurrentUserId())
                    .build());
        }

    }

    public PagedResponse<VideoResponse> getVideoWatchLaters(int page, int size) {
        Pageable pageable= PageRequest.of(page,size, Sort.by("createdAt").descending());
        Page<Video> videos = watchLaterRepository.findAllVideosByProfileId(getCurrentUserId(), pageable);
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

    private String getCurrentUserId(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Transactional
    public void delete(String videoId) {
        if(watchLaterRepository.existsByVideoIdAndProfileId(videoId, getCurrentUserId())){
            watchLaterRepository.deleteByVideoIdAndProfileId(videoId, getCurrentUserId());
        }
    }
}
