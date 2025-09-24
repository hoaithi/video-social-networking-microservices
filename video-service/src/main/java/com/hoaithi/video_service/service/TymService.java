package com.hoaithi.video_service.service;

import com.hoaithi.video_service.dto.response.ApiResponse;
import com.hoaithi.video_service.dto.response.VideoResponse;
import com.hoaithi.video_service.entity.Video;
import com.hoaithi.video_service.entity.VideoHistory;
import com.hoaithi.video_service.entity.VideoTym;
import com.hoaithi.video_service.mapper.VideoMapper;
import com.hoaithi.video_service.repository.HistoryRepository;
import com.hoaithi.video_service.repository.TymRepository;
import com.hoaithi.video_service.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class TymService {
    TymRepository tymRepository;
    VideoMapper videoMapper;
    VideoRepository videoRepository;

    public List<VideoResponse> getVideoTyms(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<VideoTym> videoTyms = tymRepository.findByUserId(authentication.getName());
        List<Video> videos = videoTyms.stream()
                .map(VideoTym::getVideo)
                .toList();
        List<VideoResponse> videoResponses = new ArrayList<>();
        for (Video video: videos) {
            VideoResponse response = videoMapper.toVideoResponse(video);
            response.setPremium(video.isPremium());
            videoResponses.add(response);
        }
        return videoResponses;
    }
    public boolean tymVideo(String videoId){
        log.info("tym video ");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<VideoTym> videoTym = tymRepository.findByUserIdAndVideoId(authentication.getName(),videoId);
        if(videoTym.isEmpty()){
            tymRepository.save(VideoTym.builder()
                    .video(videoRepository.findById(videoId).orElseThrow())
                    .userId(authentication.getName())
                    .build());
        }else{
            tymRepository.delete(videoTym.get());
        }
        return true;
    }
}
