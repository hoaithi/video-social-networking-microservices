package com.hoaithi.video_service.service;

import com.hoaithi.video_service.dto.response.VideoResponse;
import com.hoaithi.video_service.entity.Video;
import com.hoaithi.video_service.entity.VideoHistory;
import com.hoaithi.video_service.mapper.VideoMapper;
import com.hoaithi.video_service.mapper.VideoMapperImpl;
import com.hoaithi.video_service.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    public List<VideoResponse> getVideoHistories(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<VideoHistory> videoHistories = historyRepository.findByProfileId(authentication.getName());
        List<Video> videos = videoHistories.stream()
                .map(VideoHistory::getVideo)
                .toList();
        List<VideoResponse> videoResponses = new ArrayList<>();
        for (Video video: videos) {
            VideoResponse response = videoMapper.toVideoResponse(video);
            response.setPremium(video.isPremium());
            videoResponses.add(response);
        }
        return videoResponses;
    }
}
