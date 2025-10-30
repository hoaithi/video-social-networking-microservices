package com.hoaithi.video_service.service;

import com.hoaithi.video_service.dto.request.VideoCreationRequest;
import com.hoaithi.video_service.dto.request.VideoUpdationRequest;
import com.hoaithi.video_service.dto.response.*;
import com.hoaithi.video_service.entity.*;
import com.hoaithi.video_service.enums.ReactionType;
import com.hoaithi.video_service.exception.AppException;
import com.hoaithi.video_service.exception.ErrorCode;
import com.hoaithi.video_service.mapper.VideoMapper;
import com.hoaithi.video_service.repository.*;
import com.hoaithi.video_service.repository.httpclient.CommentClient;
import com.hoaithi.video_service.repository.httpclient.FileClient;
import com.hoaithi.video_service.repository.httpclient.ProfileClient;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    VideoReactionRepository videoReactionRepository;
    PlaylistRepository playlistRepository;
    ProfileClient profileClient;

    public PagedResponse<VideoResponse> getVideos(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Video> videos = videoRepository.findAll(pageable);

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
    public PagedResponse<VideoResponse> getVideoByProfileId(String profileId, int page, int size) {
        Pageable pageable= PageRequest.of(page,size,Sort.by("publishedAt").descending());
        Page<Video> videos = videoRepository.findAllByProfileId(profileId, pageable);
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
        if(!historyRepository.existsByProfileIdAndVideoId(currentProfileId, videoId)){
            historyRepository.save(VideoHistory.builder()
                    .video(video)
                    .profileId(currentProfileId)
                    .build());
        }

        VideoResponse response = videoMapper.toVideoResponse(videoRepository.save(video));
        ProfileResponse profileResponse = profileClient.getProfileById(response.getProfileId()).getResult();
        response.setProfileName(profileResponse.getFullName());
        response.setProfileImage(profileResponse.getAvatarUrl());
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


    public OwnerIdResponse getOwnerId(String videoId) {
        Video video = videoRepository.findById(videoId).orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
        return OwnerIdResponse.builder()
                .ownerId(video.getProfileId())
                .build();
    }
    public VideoResponse dislikeVideo(String videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));

        String profileId = getCurrentUserId();
        // Check if user already reacted to this video
        Optional<VideoReaction> existingReaction = videoReactionRepository.findByProfileIdAndVideoId(profileId,videoId);

        if (existingReaction.isPresent()) {
            VideoReaction reaction = existingReaction.get();

            // If already disliked, do nothing
            if (reaction.getReactionType() == ReactionType.DISLIKE) {
                videoReactionRepository.delete(reaction);
                video.setDislikeCount(video.getDislikeCount() - 1);
            }else{
                // If liked, change to dislike and update counts
                reaction.setReactionType(ReactionType.DISLIKE);
                reaction.setCreatedAt(LocalDateTime.now());
                videoReactionRepository.save(reaction);

                video.setLikeCount(video.getLikeCount() - 1);
                video.setDislikeCount(video.getDislikeCount() + 1);
            }


        } else {
            // Create new dislike reaction
            VideoReaction reaction = VideoReaction.builder()
                    .profileId(profileId)
                    .video(video)
                    .reactionType(ReactionType.DISLIKE)
                    .createdAt(LocalDateTime.now())
                    .build();

            videoReactionRepository.save(reaction);

            video.setDislikeCount(video.getDislikeCount() + 1);
        }

        VideoResponse response = videoMapper.toVideoResponse(videoRepository.save(video));
        ProfileResponse profileResponse = profileClient.getProfileById(response.getProfileId()).getResult();
        response.setProfileName(profileResponse.getFullName());
        response.setProfileImage(profileResponse.getAvatarUrl());
        return response;
    }
    @Transactional
    public VideoResponse likeVideo(String videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));

        String profileId = getCurrentUserId();
        // Check if user already reacted to this video
        Optional<VideoReaction> existingReaction = videoReactionRepository.findByProfileIdAndVideoId(profileId, videoId);

        if (existingReaction.isPresent()) {
            VideoReaction reaction = existingReaction.get();

            // If already liked, do nothing
            if (reaction.getReactionType() == ReactionType.LIKE) {
                videoReactionRepository.delete(reaction);
                video.setLikeCount(video.getLikeCount() - 1);
            }else{
                // If disliked, change to like and update counts
                reaction.setReactionType(ReactionType.LIKE);
                reaction.setCreatedAt(LocalDateTime.now());
                videoReactionRepository.save(reaction);

                video.setLikeCount(video.getLikeCount() + 1);
                video.setDislikeCount(video.getDislikeCount() - 1);
            }

        } else {
            // Create new like reaction
            VideoReaction reaction = VideoReaction.builder()
                    .profileId(profileId)
                    .video(video)
                    .reactionType(ReactionType.LIKE)
                    .createdAt(LocalDateTime.now())
                    .build();

            videoReactionRepository.save(reaction);

            video.setLikeCount(video.getLikeCount() + 1);
        }

        VideoResponse response = videoMapper.toVideoResponse(videoRepository.save(video));
        ProfileResponse profileResponse = profileClient.getProfileById(response.getProfileId()).getResult();
        response.setProfileName(profileResponse.getFullName());
        response.setProfileImage(profileResponse.getAvatarUrl());
        return response;
    }
    public VideoUserReaction getUserReaction(String videoId) {
        Optional<VideoReaction> videoReaction = videoReactionRepository.findByProfileIdAndVideoId(getCurrentUserId(), videoId);
        if (videoReaction.isPresent()) {
            return VideoUserReaction.builder()
                    .hasReacted(true)
                    .reactionType(videoReaction.get().getReactionType())
                    .createdAt(videoReaction.get().getCreatedAt())
                    .build();
        }else{
            return VideoUserReaction.builder()
                    .hasReacted(false)
                    .build();
        }
    }


}

