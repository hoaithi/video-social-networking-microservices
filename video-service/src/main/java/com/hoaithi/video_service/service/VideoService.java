//package com.hoaithi.video_service.service;
//
//import com.hoaithi.video_service.dto.request.VideoCreationRequest;
//import com.hoaithi.video_service.dto.request.VideoUpdationRequest;
//import com.hoaithi.video_service.dto.response.*;
//import com.hoaithi.video_service.entity.*;
//import com.hoaithi.video_service.enums.ReactionType;
//import com.hoaithi.video_service.exception.AppException;
//import com.hoaithi.video_service.exception.ErrorCode;
//import com.hoaithi.video_service.mapper.VideoMapper;
//import com.hoaithi.video_service.repository.*;
//import com.hoaithi.video_service.repository.httpclient.CommentClient;
//import com.hoaithi.video_service.repository.httpclient.FileClient;
//import com.hoaithi.video_service.repository.httpclient.ProfileClient;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import org.bytedeco.javacv.FFmpegFrameGrabber;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
//public class VideoService {
//    VideoRepository videoRepository;
//    VideoMapper videoMapper;
//    FileClient fileClient;
//    CommentClient commentClient;
//    HistoryRepository historyRepository;
//    VideoReactionRepository videoReactionRepository;
//    PlaylistRepository playlistRepository;
//    ProfileClient profileClient;
//
//    public PagedResponse<VideoResponse> getVideos(int page, int size){
//        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
//        Page<Video> videos = videoRepository.findAll(pageable);
//
//        Page<VideoResponse> responses = videos.map(video -> {
//            VideoResponse videoResponse = videoMapper.toVideoResponse(video);
//            videoResponse.setPremium(video.isPremium());
//            ProfileResponse profileResponse = profileClient.getProfileById(videoResponse.getProfileId()).getResult();
//            videoResponse.setProfileImage(profileResponse.getAvatarUrl());
//            videoResponse.setProfileName(profileResponse.getFullName());
//            return videoResponse;
//        });
//        return PagedResponse.<VideoResponse>builder()
//                .content(responses.getContent())
//                .page(responses.getNumber())
//                .totalPages(responses.getTotalPages())
//                .totalElements(responses.getTotalElements())
//                .size(responses.getSize())
//                .last(responses.isLast())
//                .build();
//    }
//    public PagedResponse<VideoResponse> getVideoByProfileId(String profileId, int page, int size) {
//        Pageable pageable= PageRequest.of(page,size,Sort.by("publishedAt").descending());
//        Page<Video> videos = videoRepository.findAllByProfileId(profileId, pageable);
//        Page<VideoResponse> responses = videos.map(video -> {
//            VideoResponse videoResponse = videoMapper.toVideoResponse(video);
//            videoResponse.setPremium(video.isPremium());
//            ProfileResponse profileResponse = profileClient.getProfileById(videoResponse.getProfileId()).getResult();
//            videoResponse.setProfileImage(profileResponse.getAvatarUrl());
//            videoResponse.setProfileName(profileResponse.getFullName());
//            return videoResponse;
//        });
//        return PagedResponse.<VideoResponse>builder()
//                .content(responses.getContent())
//                .page(responses.getNumber())
//                .totalPages(responses.getTotalPages())
//                .totalElements(responses.getTotalElements())
//                .size(responses.getSize())
//                .last(responses.isLast())
//                .build();
//    }
//
//    public List<VideoResponse> getVideoByPlaylist(String playlistId){
//        Playlist playlist = playlistRepository.findById(playlistId)
//                .orElseThrow(() -> new AppException(ErrorCode.PLAYLIST_NOT_EXISTED));
//
//       return playlist.getVideoPlaylists()
//                .stream()
//                .map(VideoPlaylist::getVideo)
//                .map(videoMapper::toVideoResponse)
//                .toList();
//    }
//
//    public VideoResponse getVideoById(String videoId) {
//        String currentProfileId = getCurrentUserId();
//        Video video = videoRepository.findById(videoId).orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
//        video.setViewCount(video.getViewCount()+1);
//        video.setCommentCount(commentClient.countComment(videoId).getResult().getCommentCount());
//        if(!historyRepository.existsByProfileIdAndVideoId(currentProfileId, videoId)){
//            historyRepository.save(VideoHistory.builder()
//                    .video(video)
//                    .profileId(currentProfileId)
//                    .build());
//        }
//
//        VideoResponse response = videoMapper.toVideoResponse(videoRepository.save(video));
//        ProfileResponse profileResponse = profileClient.getProfileById(response.getProfileId()).getResult();
//        response.setProfileName(profileResponse.getFullName());
//        response.setProfileImage(profileResponse.getAvatarUrl());
//        return response;
//    }
//
//
//    public VideoResponse updateVideo(String videoId, VideoUpdationRequest request) {
//        Video video = videoRepository.findById(videoId).orElseThrow(()-> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
//        if(!video.getProfileId().equals(getCurrentUserId())){
//            throw new AppException(ErrorCode.UNAUTHORIZED);
//        }
//        video.setThumbnailUrl(request.getThumbnailUrl());
//        video.setDescription(request.getDescription());
//        video.setTitle(request.getTitle());
//        return videoMapper.toVideoResponse(videoRepository.save(video));
//    }
//
//    public void deleteVideo(String id) {
//        Video video = videoRepository.findById(id)
//                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
//        if(!video.getProfileId().equals(getCurrentUserId())){
//            throw new AppException(ErrorCode.UNAUTHORIZED);
//        }
//        videoRepository.delete(video);
//    }
//
//    public VideoResponse createVideo(MultipartFile videoFile, MultipartFile thumbnailFile, VideoCreationRequest request) {
//        String currentProfileId = getCurrentUserId();
//
//        Video video = Video.builder()
//                .title(request.getTitle())
//                .description(request.getDescription())
//                .isPremium(request.isPremium())
//                .build();
//        video.setVideoUrl(fileClient.uploadFile(videoFile));
//        video.setThumbnailUrl(fileClient.uploadFile(thumbnailFile));
//        video.setPublishedAt(LocalDateTime.now());
//        video.setProfileId(currentProfileId);
//        double durationInSeconds = 0.0;
//        try {
//            // convert MultipartFile -> File tạm
//            File convFile = File.createTempFile("upload", ".mp4");
//            videoFile.transferTo(convFile);
//
//            durationInSeconds = getVideoDuration(convFile);
//
//            convFile.delete();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        video.setDuration(Math.round(durationInSeconds));
//        Video createdVideo = videoRepository.save(video);
//        VideoResponse response = videoMapper.toVideoResponse(createdVideo);
//        response.setPremium(createdVideo.isPremium());
//        return response;
//    }
//
//
//    private static double getVideoDuration(File file) throws Exception {
//        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file)) {
//            grabber.start();
//            double durationInSeconds = (double) grabber.getLengthInTime() / 1000000.0; // microseconds -> seconds
//            grabber.stop();
//            return durationInSeconds;
//        }
//    }
//    private String getCurrentUserId(){
//        return SecurityContextHolder.getContext().getAuthentication().getName();
//    }
//
//
//    public OwnerIdResponse getOwnerId(String videoId) {
//        Video video = videoRepository.findById(videoId).orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
//        return OwnerIdResponse.builder()
//                .ownerId(video.getProfileId())
//                .build();
//    }
//    public VideoResponse dislikeVideo(String videoId) {
//        Video video = videoRepository.findById(videoId)
//                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
//
//        String profileId = getCurrentUserId();
//        // Check if user already reacted to this video
//        Optional<VideoReaction> existingReaction = videoReactionRepository.findByProfileIdAndVideoId(profileId,videoId);
//
//        if (existingReaction.isPresent()) {
//            VideoReaction reaction = existingReaction.get();
//
//            // If already disliked, do nothing
//            if (reaction.getReactionType() == ReactionType.DISLIKE) {
//                videoReactionRepository.delete(reaction);
//                video.setDislikeCount(video.getDislikeCount() - 1);
//            }else{
//                // If liked, change to dislike and update counts
//                reaction.setReactionType(ReactionType.DISLIKE);
//                reaction.setCreatedAt(LocalDateTime.now());
//                videoReactionRepository.save(reaction);
//
//                video.setLikeCount(video.getLikeCount() - 1);
//                video.setDislikeCount(video.getDislikeCount() + 1);
//            }
//
//
//        } else {
//            // Create new dislike reaction
//            VideoReaction reaction = VideoReaction.builder()
//                    .profileId(profileId)
//                    .video(video)
//                    .reactionType(ReactionType.DISLIKE)
//                    .createdAt(LocalDateTime.now())
//                    .build();
//
//            videoReactionRepository.save(reaction);
//
//            video.setDislikeCount(video.getDislikeCount() + 1);
//        }
//
//        VideoResponse response = videoMapper.toVideoResponse(videoRepository.save(video));
//        ProfileResponse profileResponse = profileClient.getProfileById(response.getProfileId()).getResult();
//        response.setProfileName(profileResponse.getFullName());
//        response.setProfileImage(profileResponse.getAvatarUrl());
//        return response;
//    }
//    @Transactional
//    public VideoResponse likeVideo(String videoId) {
//        Video video = videoRepository.findById(videoId)
//                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
//
//        String profileId = getCurrentUserId();
//        // Check if user already reacted to this video
//        Optional<VideoReaction> existingReaction = videoReactionRepository.findByProfileIdAndVideoId(profileId, videoId);
//
//        if (existingReaction.isPresent()) {
//            VideoReaction reaction = existingReaction.get();
//
//            // If already liked, do nothing
//            if (reaction.getReactionType() == ReactionType.LIKE) {
//                videoReactionRepository.delete(reaction);
//                video.setLikeCount(video.getLikeCount() - 1);
//            }else{
//                // If disliked, change to like and update counts
//                reaction.setReactionType(ReactionType.LIKE);
//                reaction.setCreatedAt(LocalDateTime.now());
//                videoReactionRepository.save(reaction);
//
//                video.setLikeCount(video.getLikeCount() + 1);
//                video.setDislikeCount(video.getDislikeCount() - 1);
//            }
//
//        } else {
//            // Create new like reaction
//            VideoReaction reaction = VideoReaction.builder()
//                    .profileId(profileId)
//                    .video(video)
//                    .reactionType(ReactionType.LIKE)
//                    .createdAt(LocalDateTime.now())
//                    .build();
//
//            videoReactionRepository.save(reaction);
//
//            video.setLikeCount(video.getLikeCount() + 1);
//        }
//
//        VideoResponse response = videoMapper.toVideoResponse(videoRepository.save(video));
//        ProfileResponse profileResponse = profileClient.getProfileById(response.getProfileId()).getResult();
//        response.setProfileName(profileResponse.getFullName());
//        response.setProfileImage(profileResponse.getAvatarUrl());
//        return response;
//    }
//    public VideoUserReaction getUserReaction(String videoId) {
//        Optional<VideoReaction> videoReaction = videoReactionRepository.findByProfileIdAndVideoId(getCurrentUserId(), videoId);
//        if (videoReaction.isPresent()) {
//            return VideoUserReaction.builder()
//                    .hasReacted(true)
//                    .reactionType(videoReaction.get().getReactionType())
//                    .createdAt(videoReaction.get().getCreatedAt())
//                    .build();
//        }else{
//            return VideoUserReaction.builder()
//                    .hasReacted(false)
//                    .build();
//        }
//    }
//
//
//}











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
        log.info("=== Getting Videos - Page: {}, Size: {} ===", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Video> videos = videoRepository.findAll(pageable);

        log.info("Found {} videos for page {}", videos.getContent().size(), page);

        Page<VideoResponse> responses = videos.map(video -> {
            VideoResponse videoResponse = videoMapper.toVideoResponse(video);
            videoResponse.setPremium(video.isPremium());
            ProfileResponse profileResponse = profileClient.getProfileById(videoResponse.getProfileId()).getResult();
            videoResponse.setProfileImage(profileResponse.getAvatarUrl());
            videoResponse.setProfileName(profileResponse.getFullName());
            return videoResponse;
        });

        log.info("=== Videos Retrieved Successfully ===");

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
        log.info("=== Getting Videos by Profile - ProfileId: {}, Page: {}, Size: {} ===", profileId, page, size);

        Pageable pageable= PageRequest.of(page,size,Sort.by("publishedAt").descending());
        Page<Video> videos = videoRepository.findAllByProfileId(profileId, pageable);

        log.info("Found {} videos for profile: {}", videos.getContent().size(), profileId);

        Page<VideoResponse> responses = videos.map(video -> {
            VideoResponse videoResponse = videoMapper.toVideoResponse(video);
            videoResponse.setPremium(video.isPremium());
            ProfileResponse profileResponse = profileClient.getProfileById(videoResponse.getProfileId()).getResult();
            videoResponse.setProfileImage(profileResponse.getAvatarUrl());
            videoResponse.setProfileName(profileResponse.getFullName());
            return videoResponse;
        });

        log.info("=== Profile Videos Retrieved Successfully ===");

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
        log.info("Getting videos by playlist: {}", playlistId);

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAYLIST_NOT_EXISTED));

        List<VideoResponse> responses = playlist.getVideoPlaylists()
                .stream()
                .map(VideoPlaylist::getVideo)
                .map(videoMapper::toVideoResponse)
                .toList();

        log.info("Found {} videos in playlist: {}", responses.size(), playlistId);

        return responses;
    }

    public VideoResponse getVideoById(String videoId) {
        log.info("=== Getting Video by ID: {} ===", videoId);

        String currentProfileId = getCurrentUserId();
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
        boolean hasMembership = profileClient.checkMembership(video.getProfileId()).getResult();
        if(video.isPremium() && !hasMembership){
            log.warn("Access denied to premium video: {} for profile: {}", videoId, currentProfileId);
            throw new AppException(ErrorCode.PREMIUM_VIDEO_ACCESS_DENIED);
        }

        log.info("Video found - Title: {}, Current views: {}", video.getTitle(), video.getViewCount());

        video.setViewCount(video.getViewCount()+1);
        video.setCommentCount(commentClient.countComment(videoId).getResult().getCommentCount());

        log.info("View count updated to: {}", video.getViewCount());

        if(!historyRepository.existsByProfileIdAndVideoId(currentProfileId, videoId)){
            historyRepository.save(VideoHistory.builder()
                    .video(video)
                    .profileId(currentProfileId)
                    .build());
            log.info("Video history recorded for profile: {}", currentProfileId);
        }

        VideoResponse response = videoMapper.toVideoResponse(videoRepository.save(video));
        ProfileResponse profileResponse = profileClient.getProfileById(response.getProfileId()).getResult();
        response.setProfileName(profileResponse.getFullName());
        response.setProfileImage(profileResponse.getAvatarUrl());

        log.info("=== Video Retrieved Successfully ===");

        return response;
    }

    public VideoResponse updateVideo(String videoId, VideoUpdationRequest request) {
        log.info("=== Updating Video: {} ===", videoId);

        Video video = videoRepository.findById(videoId)
                .orElseThrow(()-> new AppException(ErrorCode.VIDEO_NOT_EXISTED));

        if(!video.getProfileId().equals(getCurrentUserId())){
            log.warn("Unauthorized update attempt by profile: {}", getCurrentUserId());
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        video.setThumbnailUrl(request.getThumbnailUrl());
        video.setDescription(request.getDescription());
        video.setTitle(request.getTitle());

        log.info("Video updated - New title: {}", request.getTitle());

        return videoMapper.toVideoResponse(videoRepository.save(video));
    }

    public void deleteVideo(String id) {
        log.info("=== Deleting Video: {} ===", id);

        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));

        if(!video.getProfileId().equals(getCurrentUserId())){
            log.warn("Unauthorized delete attempt by profile: {}", getCurrentUserId());
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        videoRepository.delete(video);
        log.info("=== Video Deleted Successfully ===");
    }

    @Transactional
    public VideoResponse createVideo(MultipartFile videoFile, MultipartFile thumbnailFile, VideoCreationRequest request) {
        log.info("=== Creating New Video ===");
        log.info("Title: {}", request.getTitle());
        log.info("Video file size: {} bytes ({} MB)",
                videoFile.getSize(), videoFile.getSize() / (1024.0 * 1024.0));
        log.info("Thumbnail file size: {} bytes", thumbnailFile.getSize());
        log.info("Is Premium: {}", request.isPremium());

        String currentProfileId = getCurrentUserId();
        log.info("Current profile ID: {}", currentProfileId);

        Video video = Video.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .isPremium(request.isPremium())
                .build();

        // Upload video file
        log.info("Starting video file upload to S3...");
        long videoUploadStart = System.currentTimeMillis();
        String videoUrl = fileClient.uploadFile(videoFile);
        long videoUploadTime = System.currentTimeMillis() - videoUploadStart;
        video.setVideoUrl(videoUrl);
        log.info("Video file uploaded in {} ms - URL: {}", videoUploadTime, videoUrl);

        // Upload thumbnail file
        log.info("Starting thumbnail file upload to S3...");
        long thumbnailUploadStart = System.currentTimeMillis();
        String thumbnailUrl = fileClient.uploadFile(thumbnailFile);
        long thumbnailUploadTime = System.currentTimeMillis() - thumbnailUploadStart;
        video.setThumbnailUrl(thumbnailUrl);
        log.info("Thumbnail uploaded in {} ms - URL: {}", thumbnailUploadTime, thumbnailUrl);

        video.setPublishedAt(LocalDateTime.now());
        video.setProfileId(currentProfileId);

        // Calculate video duration
        double durationInSeconds = 0.0;
        try {
            log.info("Calculating video duration...");
            // Convert MultipartFile -> File tạm
            File convFile = File.createTempFile("upload", ".mp4");
            videoFile.transferTo(convFile);

            durationInSeconds = getVideoDuration(convFile);
            log.info("Video duration: {} seconds ({} minutes)",
                    durationInSeconds, durationInSeconds / 60.0);

            convFile.delete();
            log.info("Temporary file deleted");
        } catch (Exception e) {
            log.error("Error calculating video duration", e);
        }

        video.setDuration(Math.round(durationInSeconds));
        Video createdVideo = videoRepository.save(video);

        log.info("=== Video Created Successfully ===");
        log.info("Video ID: {}", createdVideo.getId());
        log.info("Total upload time: {} ms", videoUploadTime + thumbnailUploadTime);

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
        log.info("Getting owner ID for video: {}", videoId);

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));

        return OwnerIdResponse.builder()
                .ownerId(video.getProfileId())
                .build();
    }

    public VideoResponse dislikeVideo(String videoId) {
        log.info("=== Processing Dislike for Video: {} ===", videoId);

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));

        String profileId = getCurrentUserId();
        Optional<VideoReaction> existingReaction = videoReactionRepository
                .findByProfileIdAndVideoId(profileId, videoId);

        if (existingReaction.isPresent()) {
            VideoReaction reaction = existingReaction.get();

            if (reaction.getReactionType() == ReactionType.DISLIKE) {
                log.info("Removing existing dislike - Profile: {}", profileId);
                videoReactionRepository.delete(reaction);
                video.setDislikeCount(video.getDislikeCount() - 1);
            } else {
                log.info("Changing from LIKE to DISLIKE - Profile: {}", profileId);
                reaction.setReactionType(ReactionType.DISLIKE);
                reaction.setCreatedAt(LocalDateTime.now());
                videoReactionRepository.save(reaction);

                video.setLikeCount(video.getLikeCount() - 1);
                video.setDislikeCount(video.getDislikeCount() + 1);
            }
        } else {
            log.info("Creating new DISLIKE reaction - Profile: {}", profileId);
            VideoReaction reaction = VideoReaction.builder()
                    .profileId(profileId)
                    .video(video)
                    .reactionType(ReactionType.DISLIKE)
                    .createdAt(LocalDateTime.now())
                    .build();

            videoReactionRepository.save(reaction);
            video.setDislikeCount(video.getDislikeCount() + 1);
        }

        log.info("Dislike processed - Likes: {}, Dislikes: {}",
                video.getLikeCount(), video.getDislikeCount());

        VideoResponse response = videoMapper.toVideoResponse(videoRepository.save(video));
        ProfileResponse profileResponse = profileClient.getProfileById(response.getProfileId()).getResult();
        response.setProfileName(profileResponse.getFullName());
        response.setProfileImage(profileResponse.getAvatarUrl());

        log.info("=== Dislike Completed Successfully ===");

        return response;
    }

    @Transactional
    public VideoResponse likeVideo(String videoId) {
        log.info("=== Processing Like for Video: {} ===", videoId);

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));

        String profileId = getCurrentUserId();
        Optional<VideoReaction> existingReaction = videoReactionRepository
                .findByProfileIdAndVideoId(profileId, videoId);

        if (existingReaction.isPresent()) {
            VideoReaction reaction = existingReaction.get();

            if (reaction.getReactionType() == ReactionType.LIKE) {
                log.info("Removing existing like - Profile: {}", profileId);
                videoReactionRepository.delete(reaction);
                video.setLikeCount(video.getLikeCount() - 1);
            } else {
                log.info("Changing from DISLIKE to LIKE - Profile: {}", profileId);
                reaction.setReactionType(ReactionType.LIKE);
                reaction.setCreatedAt(LocalDateTime.now());
                videoReactionRepository.save(reaction);

                video.setLikeCount(video.getLikeCount() + 1);
                video.setDislikeCount(video.getDislikeCount() - 1);
            }
        } else {
            log.info("Creating new LIKE reaction - Profile: {}", profileId);
            VideoReaction reaction = VideoReaction.builder()
                    .profileId(profileId)
                    .video(video)
                    .reactionType(ReactionType.LIKE)
                    .createdAt(LocalDateTime.now())
                    .build();

            videoReactionRepository.save(reaction);
            video.setLikeCount(video.getLikeCount() + 1);
        }

        log.info("Like processed - Likes: {}, Dislikes: {}",
                video.getLikeCount(), video.getDislikeCount());

        VideoResponse response = videoMapper.toVideoResponse(videoRepository.save(video));
        ProfileResponse profileResponse = profileClient.getProfileById(response.getProfileId()).getResult();
        response.setProfileName(profileResponse.getFullName());
        response.setProfileImage(profileResponse.getAvatarUrl());

        log.info("=== Like Completed Successfully ===");

        return response;
    }

    public VideoUserReaction getUserReaction(String videoId) {
        log.info("Getting user reaction for video: {}", videoId);

        Optional<VideoReaction> videoReaction = videoReactionRepository
                .findByProfileIdAndVideoId(getCurrentUserId(), videoId);

        if (videoReaction.isPresent()) {
            log.info("User has reacted: {}", videoReaction.get().getReactionType());
            return VideoUserReaction.builder()
                    .hasReacted(true)
                    .reactionType(videoReaction.get().getReactionType())
                    .createdAt(videoReaction.get().getCreatedAt())
                    .build();
        } else {
            log.info("User has not reacted to this video");
            return VideoUserReaction.builder()
                    .hasReacted(false)
                    .build();
        }
    }
}