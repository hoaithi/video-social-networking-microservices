package com.hoaithi.video_service.service;

import com.hoaithi.video_service.dto.request.VideoCreationRequest;
import com.hoaithi.video_service.dto.request.VideoUpdationRequest;
import com.hoaithi.video_service.dto.response.PagedResponse;
import com.hoaithi.video_service.dto.response.VideoResponse;
import com.hoaithi.video_service.entity.Playlist;
import com.hoaithi.video_service.entity.Video;
import com.hoaithi.video_service.entity.VideoHistory;
import com.hoaithi.video_service.entity.VideoPlaylist;
import com.hoaithi.video_service.exception.AppException;
import com.hoaithi.video_service.exception.ErrorCode;
import com.hoaithi.video_service.mapper.VideoMapper;
import com.hoaithi.video_service.repository.HistoryRepository;
import com.hoaithi.video_service.repository.PlaylistRepository;
import com.hoaithi.video_service.repository.VideoHeartRepository;
import com.hoaithi.video_service.repository.httpclient.CommentClient;
import com.hoaithi.video_service.repository.httpclient.FileClient;
import com.hoaithi.video_service.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class VideoService {
    VideoRepository videoRepository;
    VideoMapper videoMapper;
    FileClient fileClient;
    CommentClient commentClient;
    HistoryRepository historyRepository;
    VideoHeartRepository videoHeartRepository;
    PlaylistRepository playlistRepository;

    public PagedResponse<VideoResponse> getVideos(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Video> videos = videoRepository.findAll(pageable);

        Page<VideoResponse> responses = videos.map(video -> {
            VideoResponse videoResponse = videoMapper.toVideoResponse(video);
            videoResponse.setPremium(video.isPremium());
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
    public PagedResponse<VideoResponse> getVideoByProfileId(String profileId, int page, int size) {
        Pageable pageable= PageRequest.of(page,size,Sort.by("publishedAt").descending());
        Page<Video> videos = videoRepository.findAllByProfileId(profileId, pageable);
        Page<VideoResponse> responses = videos.map(video -> {
            VideoResponse videoResponse = videoMapper.toVideoResponse(video);
            videoResponse.setPremium(video.isPremium());
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

    public List<VideoResponse> getVideoByPlaylist(String playlistId){
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAYLIST_NOT_EXISTED));

       return playlist.getVideoPlaylists()
                .stream()
                .map(VideoPlaylist::getVideo)
                .map(videoMapper::toVideoResponse)
                .toList();
    }

    public VideoResponse getVideoById(String videoId) {
        String currentProfileId = getCurrentUserId();
        Video video = videoRepository.findById(videoId).orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
        video.setViewCount(video.getViewCount()+1);
        video.setCommentCount(commentClient.countComment(videoId).getResult().getCommentCount());
        historyRepository.save(VideoHistory.builder()
                .video(video)
                .profileId(currentProfileId)
                .build());
        VideoResponse response = videoMapper.toVideoResponse(videoRepository.save(video));
        response.setHearted(videoHeartRepository.existsByProfileIdAndVideoId(currentProfileId, videoId));
        return response;
    }


    public VideoResponse updateVideo(String videoId, VideoUpdationRequest request) {
        Video video = videoRepository.findById(videoId).orElseThrow(()-> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
        if(!video.getProfileId().equals(getCurrentUserId())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        video.setThumbnailUrl(request.getThumbnailUrl());
        video.setDescription(request.getDescription());
        video.setTitle(request.getTitle());
        return videoMapper.toVideoResponse(videoRepository.save(video));
    }

    public void deleteVideo(String id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
        if(!video.getProfileId().equals(getCurrentUserId())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        videoRepository.delete(video);
    }

    public VideoResponse createVideo(MultipartFile videoFile, MultipartFile thumbnailFile, VideoCreationRequest request) {
        String currentProfileId = getCurrentUserId();

        Video video = Video.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .isPremium(request.isPremium())
                .build();
        video.setVideoUrl(fileClient.uploadFile(videoFile));
        video.setThumbnailUrl(fileClient.uploadFile(thumbnailFile));
        video.setPublishedAt(LocalDateTime.now());
        video.setProfileId(currentProfileId);
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
        Video createdVideo = videoRepository.save(video);
        VideoResponse response = videoMapper.toVideoResponse(createdVideo);
        response.setPremium(createdVideo.isPremium());
        return response;
    }


    private static double getVideoDuration(File file) throws Exception {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file)) {
            grabber.start();
            double durationInSeconds = (double) grabber.getLengthInTime() / 1000000.0; // microseconds -> seconds
            grabber.stop();
            return durationInSeconds;
        }
    }
    private String getCurrentUserId(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }



}

