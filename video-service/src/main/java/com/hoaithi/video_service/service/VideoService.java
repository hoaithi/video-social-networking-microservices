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

import com.hoaithi.event.dto.VideoUploadedEvent;
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
import com.hoaithi.video_service.repository.httpclient.SubClient;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class VideoService {
    VideoRepository videoRepository;
    VideoMapper videoMapper;
    FileClient fileClient;
    FileStorageService fileStorageService;
    CommentClient commentClient;
    HistoryRepository historyRepository;
    VideoReactionRepository videoReactionRepository;
    PlaylistRepository playlistRepository;
    ProfileClient profileClient;
    SubClient subClient;
    KafkaTemplate<String, Object> kafkaTemplate;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");
    public PagedResponse<VideoResponse> searchVideos(
            String keyword,
            Boolean isPremium,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        log.info("=== Searching Videos - Keyword: {}, isPremium: {}, Page: {}, Size: {} ===",
                keyword, isPremium, page, size);

        // Tạo Sort object
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // Tìm kiếm videos với các điều kiện
        Page<Video> videos;

        if (keyword != null && !keyword.trim().isEmpty() && isPremium != null) {
            // Cả keyword và isPremium
            videos = videoRepository.findByTitleContainingIgnoreCaseAndIsPremium(keyword, isPremium, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            // Chỉ có keyword
            videos = videoRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        } else if (isPremium != null) {
            // Chỉ có isPremium
            videos = videoRepository.findByIsPremium(isPremium, pageable);
        } else {
            // Không có filter nào
            videos = videoRepository.findAll(pageable);
        }

        log.info("Found {} videos matching criteria", videos.getContent().size());

        // Map sang VideoResponse và thêm thông tin profile
        Page<VideoResponse> responses = videos.map(video -> {
            VideoResponse videoResponse = videoMapper.toVideoResponse(video);
            videoResponse.setPremium(video.isPremium());

            try {
                ProfileResponse profileResponse = profileClient.getProfileById(videoResponse.getProfileId()).getResult();
                videoResponse.setProfileImage(profileResponse.getAvatarUrl());
                videoResponse.setProfileName(profileResponse.getFullName());
            } catch (Exception e) {
                log.error("Error fetching profile for video {}: {}", video.getId(), e.getMessage());
            }

            return videoResponse;
        });

        log.info("=== Videos Search Completed Successfully ===");

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
        if(!currentProfileId.equals(video.getProfileId())){
            if(video.isPremium() && !hasMembership){
                log.warn("Access denied to premium video: {} for profile: {}", videoId, currentProfileId);
                throw new AppException(ErrorCode.PREMIUM_VIDEO_ACCESS_DENIED);
            }
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

        // push event


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
        SecurityContextHolder.getContext().getAuthentication();

        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));

//        if(!video.getProfileId().equals(getCurrentUserId())){
//            log.warn("Unauthorized delete attempt by profile: {}", getCurrentUserId());
//            throw new AppException(ErrorCode.UNAUTHORIZED);
//        }

        videoRepository.delete(video);
        log.info("=== Video Deleted Successfully ===");
    }

    @Transactional
    public VideoResponse createVideo(MultipartFile videoFile, MultipartFile thumbnailFile, VideoCreationRequest request) throws IOException {
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
//        String videoUrl = fileClient.uploadFile(videoFile);
        String videoUrl = fileStorageService.generatePresignedUrl(fileStorageService.uploadFile(videoFile));
        long videoUploadTime = System.currentTimeMillis() - videoUploadStart;
        video.setVideoUrl(videoUrl);
        log.info("Video file uploaded in {} ms - URL: {}", videoUploadTime, videoUrl);

        // Upload thumbnail file
        log.info("Starting thumbnail file upload to S3...");
        long thumbnailUploadStart = System.currentTimeMillis();
        String thumbnailUrl = fileStorageService.generatePresignedUrl(fileStorageService.uploadFile(thumbnailFile));
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

        ProfileResponse profileResponse = profileClient.getProfileById(response.getProfileId()).getResult();
        VideoUploadedEvent event = VideoUploadedEvent.builder()
                .videoId(video.getId())
                .channelId(video.getProfileId())
                .title(video.getTitle())
                .thumbnailUrl(video.getThumbnailUrl())
                .description(video.getDescription())
                .uploadedAt(LocalDateTime.now())
                .avatarUrl(profileResponse.getAvatarUrl())
                .fullName(profileResponse.getFullName())
                .build();
        kafkaTemplate.send("video-upload", event);
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

    public DashboardResponse getDashboardStatsByProfile(String profileId) {
        log.info("=== Calculating Dashboard Stats for Profile: {} ===", profileId);

        // Step 1: Get all videos by profileId
        List<Video> profileVideos = videoRepository.findAllByProfileId(profileId);
        log.info("Found {} videos for profile: {}", profileVideos.size(), profileId);

        // Step 2: Calculate total stats
        long totalViews = profileVideos.stream()
                .mapToLong(Video::getViewCount)
                .sum();
        long totalLikes = profileVideos.stream()
                .mapToLong(Video::getLikeCount)
                .sum();
        long totalDislikes = profileVideos.stream()
                .mapToLong(Video::getDislikeCount)
                .sum();
        long totalComments = profileVideos.stream()
                .mapToLong(Video::getCommentCount)
                .sum();

        log.info("Stats calculated - Views: {}, Likes: {}, Dislikes: {}, Comments: {}",
                totalViews, totalLikes, totalDislikes, totalComments);

        Stats stats = Stats.builder()
                .totalViews(totalViews)
                .totalLikes(totalLikes)
                .totalDislikes(totalDislikes)
                .totalComments(totalComments)
                .totalVideos((long) profileVideos.size())
                .build();

        // Step 3: Get subscriber count from Profile Service
        Long subscriberCount = 0L;
        try {
            subscriberCount = subClient.getSubscriberCount(profileId).getResult();
            log.info("Subscriber count: {}", subscriberCount);
        } catch (Exception e) {
            log.error("Error getting subscriber count: {}", e.getMessage());
        }

        // Step 4: Get top 5 videos based on engagement (views + likes)
        List<TopVideo> topVideos = profileVideos.stream()
                .sorted(Comparator
                        .comparingLong((Video v) -> v.getViewCount() + v.getLikeCount())
                        .reversed())
                .limit(5)
                .map(video -> mapToTopVideo(video, profileId))
                .toList();

        log.info("Top {} videos selected", topVideos.size());

        // Step 5: Build response
        return DashboardResponse.builder()
                .stats(stats)
                .totalUsers(subscriberCount)
                .topVideos(topVideos)
                .build();
    }

    public DashboardResponse getAdminDashboardStats() {
        log.info("=== Admin: Calculating Overall Dashboard Stats ===");

        // Step 1: Get ALL videos from all users
        List<Video> allVideos = videoRepository.findAll();
        log.info("Found {} total videos across all users", allVideos.size());

        // Step 2: Calculate total stats for all videos
        long totalViews = allVideos.stream()
                .mapToLong(Video::getViewCount)
                .sum();
        long totalLikes = allVideos.stream()
                .mapToLong(Video::getLikeCount)
                .sum();
        long totalDislikes = allVideos.stream()
                .mapToLong(Video::getDislikeCount)
                .sum();
        long totalComments = allVideos.stream()
                .mapToLong(Video::getCommentCount)
                .sum();

        log.info("Overall Stats - Views: {}, Likes: {}, Dislikes: {}, Comments: {}",
                totalViews, totalLikes, totalDislikes, totalComments);

        Stats stats = Stats.builder()
                .totalViews(totalViews)
                .totalLikes(totalLikes)
                .totalDislikes(totalDislikes)
                .totalComments(totalComments)
                .totalVideos((long) allVideos.size())
                .build();

        // Step 3: Get total user count from Profile Service
        Long totalUsers = 0L;
        try {
            totalUsers = profileClient.getTotalUserCount().getResult();
            log.info("Total users in system: {}", totalUsers);
        } catch (Exception e) {
            log.error("Error getting total user count: {}", e.getMessage());
        }

        // Step 4: Get top 5 videos based on engagement (views + likes) from ALL videos
        List<TopVideo> topVideos = allVideos.stream()
                .sorted(Comparator
                        .comparingLong((Video v) -> v.getViewCount() + v.getLikeCount())
                        .reversed())
                .limit(5)
                .map(this::mapToTopVideoWithProfileFetch)
                .toList();

        log.info("Top {} videos selected from all videos", topVideos.size());

        // Step 5: Build response
        return DashboardResponse.builder()
                .stats(stats)
                .totalUsers(totalUsers)
                .topVideos(topVideos)
                .build();
    }


    public PagedResponse<VideoResponse> getAllVideosForAdmin(
            int page, int size, String search, Boolean isPremium, String sortBy, String sortDirection) {

        log.info("=== Getting All Videos - Filters: search={}, isPremium={}, sort={} {} ===",
                search, isPremium, sortBy, sortDirection);

        // Build Sort
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);

        // Get videos with filters
        Page<Video> videoPage;

        if (search != null && !search.isEmpty() && isPremium != null) {
            // Filter by both search and isPremium
            videoPage = videoRepository.findByTitleContainingIgnoreCaseAndIsPremium(
                    search, isPremium, pageable);
            log.info("Applied filters: search + isPremium");
        } else if (search != null && !search.isEmpty()) {
            // Filter by search only
            videoPage = videoRepository.findByTitleContainingIgnoreCase(search, pageable);
            log.info("Applied filter: search only");
        } else if (isPremium != null) {
            // Filter by isPremium only
            videoPage = videoRepository.findByIsPremium(isPremium, pageable);
            log.info("Applied filter: isPremium only");
        } else {
            // No filters
            videoPage = videoRepository.findAll(pageable);
            log.info("No filters applied");
        }

        log.info("Found {} videos on page {} of {}",
                videoPage.getContent().size(), page, videoPage.getTotalPages());

        // Map to VideoResponse with profile info
        List<VideoResponse> videoResponses = videoPage.getContent().stream()
                .map(video -> {
                    VideoResponse response = mapToVideoResponse(video);

                    // Fetch profile info
                    try {
                        ProfileResponse profile = profileClient.getProfileById(video.getProfileId()).getResult();
                        response.setProfileImage(profile.getAvatarUrl());
                        response.setProfileName(profile.getFullName());
                    } catch (Exception e) {
                        log.error("Error fetching profile for video {}: {}", video.getId(), e.getMessage());
                    }

                    return response;
                })
                .toList();

        return PagedResponse.<VideoResponse>builder()
                .content(videoResponses)
                .page(videoPage.getNumber())
                .size(videoPage.getSize())
                .totalElements(videoPage.getTotalElements())
                .totalPages(videoPage.getTotalPages())
                .last(videoPage.isLast())
                .build();
    }

    private VideoResponse mapToVideoResponse(Video video) {
        return VideoResponse.builder()
                .id(video.getId())
                .title(video.getTitle())
                .description(video.getDescription())
                .duration(video.getDuration())
                .viewCount(video.getViewCount())
                .thumbnailUrl(video.getThumbnailUrl())
                .videoUrl(video.getVideoUrl())
                .profileId(video.getProfileId())
//                .publishedAt(video.getPublishedAt().toString())
                .likeCount(video.getLikeCount())
                .dislikeCount(video.getDislikeCount())
                .commentCount(video.getCommentCount())
                .isPremium(video.isPremium())
                .build();
    }

    private TopVideo mapToTopVideo(Video video, String profileId) {
        log.info("Mapping video to TopVideo: {}", video.getId());

        // Get profile information
        ProfileResponse profile = null;
        try {
            profile = profileClient.getProfileById(profileId).getResult();
        } catch (Exception e) {
            log.error("Error getting profile info: {}", e.getMessage());
        }

        // Calculate engagement score (simple formula: views + likes * 2 - dislikes)
        double engagementScore = video.getViewCount()
                + (video.getLikeCount() * 2.0)
                - video.getDislikeCount();

        return TopVideo.builder()
                .id(video.getId())
                .title(video.getTitle())
                .description(video.getDescription())
                .viewCount(video.getViewCount())
                .thumbnailUrl(video.getThumbnailUrl())
                .videoUrl(video.getVideoUrl())
                .profileId(video.getProfileId())
                .profileImage(profile != null ? profile.getAvatarUrl() : null)
                .profileName(profile != null ? profile.getFullName() : null)
                .publishedAt(video.getPublishedAt().toString())
                .likeCount(video.getLikeCount())
                .dislikeCount(video.getDislikeCount())
                .commentCount(video.getCommentCount())
                .hearted(false)
                .engagementScore(engagementScore)
                .isPremium(video.isPremium())
                .build();
    }

    private TopVideo mapToTopVideoWithProfileFetch(Video video) {
        log.info("Mapping video to TopVideo with profile fetch: {}", video.getId());

        // Get profile information using video's profileId
        ProfileResponse profile = null;
        try {
            profile = profileClient.getProfileById(video.getProfileId()).getResult();
        } catch (Exception e) {
            log.error("Error getting profile info for video {}: {}", video.getId(), e.getMessage());
        }

        // Calculate engagement score
        double engagementScore = video.getViewCount()
                + (video.getLikeCount() * 2.0)
                - video.getDislikeCount();

        return TopVideo.builder()
                .id(video.getId())
                .title(video.getTitle())
                .description(video.getDescription())
                .viewCount(video.getViewCount())
                .thumbnailUrl(video.getThumbnailUrl())
                .videoUrl(video.getVideoUrl())
                .profileId(video.getProfileId())
                .profileImage(profile != null ? profile.getAvatarUrl() : null)
                .profileName(profile != null ? profile.getFullName() : null)
                .publishedAt(video.getPublishedAt().toString())
                .likeCount(video.getLikeCount())
                .dislikeCount(video.getDislikeCount())
                .commentCount(video.getCommentCount())
                .hearted(false)
                .engagementScore(engagementScore)
                .isPremium(video.isPremium())
                .build();
    }



    public VideoMonthlyStatsResponse getMonthlyStatsByProfileId(String profileId, Integer months) {
        log.info("=== Getting Monthly Video Stats for Profile: {} (Last {} months) ===", profileId, months);

        if (months == null || months <= 0) {
            months = 6;
        }

        // Calculate date range
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(months);

        // Get video date range
        LocalDateTime firstVideoDate = startDate;
        LocalDateTime lastVideoDate = endDate;

        try {
            Object[] dateRange = videoRepository.getVideoDateRange(profileId);
            if (dateRange != null && dateRange.length >= 2) {
                if (dateRange[0] != null) {
                    firstVideoDate = (LocalDateTime) dateRange[0];
                }
                if (dateRange[1] != null) {
                    lastVideoDate = (LocalDateTime) dateRange[1];
                }
            }
            log.info("Video date range: {} to {}", firstVideoDate, lastVideoDate);
        } catch (Exception e) {
            log.warn("Error getting video date range: {}", e.getMessage());
        }

        // Initialize map for monthly stats
        Map<String, MonthlyVideoStatsDTO> monthlyStatsMap = new HashMap<>();

        // Step 1: Get monthly video uploads
        try {
            List<Object[]> videoUploadData = videoRepository.getMonthlyVideoUploadsByProfileId(profileId);
            if (videoUploadData != null && !videoUploadData.isEmpty()) {
                log.info("Found {} months of video upload data", videoUploadData.size());

                for (Object[] row : videoUploadData) {
                    try {
                        Integer year = row[0] != null ? (Integer) row[0] : null;
                        Integer month = row[1] != null ? (Integer) row[1] : null;
                        Long videoCount = row[2] != null ? ((Number) row[2]).longValue() : 0L;

                        if (year != null && month != null) {
                            String monthKey = String.format("%d-%02d", year, month);
                            YearMonth yearMonth = YearMonth.of(year, month);

                            MonthlyVideoStatsDTO stats = MonthlyVideoStatsDTO.builder()
                                    .month(yearMonth.format(MONTH_FORMATTER))
                                    .year(year)
                                    .monthNumber(month)
                                    .newVideos(videoCount)
                                    .totalVideos(0L)
                                    .newViews(0L)
                                    .totalViews(0L)
                                    .newLikes(0L)
                                    .totalLikes(0L)
                                    .newComments(0L)
                                    .totalComments(0L)
                                    .averageEngagementRate(0.0)
                                    .newSubscribers(0L)
                                    .build();

                            monthlyStatsMap.put(monthKey, stats);
                        }
                    } catch (Exception e) {
                        log.error("Error processing video upload row: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error getting monthly video uploads: {}", e.getMessage());
        }

        // Step 2: Get monthly engagement stats
        try {
            List<Object[]> engagementData = videoRepository.getMonthlyEngagementByProfileId(profileId);
            if (engagementData != null && !engagementData.isEmpty()) {
                log.info("Found {} months of engagement data", engagementData.size());

                for (Object[] row : engagementData) {
                    try {
                        Integer year = row[0] != null ? (Integer) row[0] : null;
                        Integer month = row[1] != null ? (Integer) row[1] : null;
                        Long views = row[2] != null ? ((Number) row[2]).longValue() : 0L;
                        Long likes = row[3] != null ? ((Number) row[3]).longValue() : 0L;
                        Long dislikes = row[4] != null ? ((Number) row[4]).longValue() : 0L;
                        Long comments = row[5] != null ? ((Number) row[5]).longValue() : 0L;

                        if (year != null && month != null) {
                            String monthKey = String.format("%d-%02d", year, month);
                            YearMonth yearMonth = YearMonth.of(year, month);

                            MonthlyVideoStatsDTO stats = monthlyStatsMap.get(monthKey);
                            if (stats != null) {
                                stats.setNewViews(views);
                                stats.setNewLikes(likes);
                                stats.setNewComments(comments);
                            } else {
                                stats = MonthlyVideoStatsDTO.builder()
                                        .month(yearMonth.format(MONTH_FORMATTER))
                                        .year(year)
                                        .monthNumber(month)
                                        .newVideos(0L)
                                        .newViews(views)
                                        .newLikes(likes)
                                        .newComments(comments)
                                        .build();
                                monthlyStatsMap.put(monthKey, stats);
                            }

                            // Calculate engagement rate
                            if (views > 0) {
                                double engagementRate = ((likes + comments) * 100.0) / views;
                                stats.setAverageEngagementRate(Math.round(engagementRate * 100.0) / 100.0);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error processing engagement row: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error getting monthly engagement: {}", e.getMessage());
        }

        // Step 3: Fill in missing months and calculate cumulative values
        List<MonthlyVideoStatsDTO> completeMonthlyData = new ArrayList<>();
        YearMonth currentYearMonth = YearMonth.from(startDate);
        YearMonth endYearMonth = YearMonth.from(endDate);

        Long cumulativeVideos = 0L;
        Long cumulativeViews = 0L;
        Long cumulativeLikes = 0L;
        Long cumulativeComments = 0L;

        while (!currentYearMonth.isAfter(endYearMonth)) {
            String monthKey = String.format("%d-%02d",
                    currentYearMonth.getYear(),
                    currentYearMonth.getMonthValue());

            MonthlyVideoStatsDTO stats = monthlyStatsMap.get(monthKey);

            if (stats == null) {
                stats = MonthlyVideoStatsDTO.builder()
                        .month(currentYearMonth.format(MONTH_FORMATTER))
                        .year(currentYearMonth.getYear())
                        .monthNumber(currentYearMonth.getMonthValue())
                        .newVideos(0L)
                        .newViews(0L)
                        .newLikes(0L)
                        .newComments(0L)
                        .averageEngagementRate(0.0)
                        .newSubscribers(0L)
                        .build();
            }

            // Calculate cumulative values
            cumulativeVideos += stats.getNewVideos() != null ? stats.getNewVideos() : 0L;
            cumulativeViews += stats.getNewViews() != null ? stats.getNewViews() : 0L;
            cumulativeLikes += stats.getNewLikes() != null ? stats.getNewLikes() : 0L;
            cumulativeComments += stats.getNewComments() != null ? stats.getNewComments() : 0L;

            stats.setTotalVideos(cumulativeVideos);
            stats.setTotalViews(cumulativeViews);
            stats.setTotalLikes(cumulativeLikes);
            stats.setTotalComments(cumulativeComments);

            completeMonthlyData.add(stats);
            currentYearMonth = currentYearMonth.plusMonths(1);
        }

        // Step 4: Get overall stats and subscriber count
        Stats overallStats = calculateOverallStats(profileId);
        Long subscriberCount = 0L;
        try {
            subscriberCount = subClient.getSubscriberCount(profileId).getResult();
            log.info("Subscriber count: {}", subscriberCount);
        } catch (Exception e) {
            log.error("Error getting subscriber count: {}", e.getMessage());
        }

        log.info("Generated {} months of data, total videos: {}, total views: {}",
                completeMonthlyData.size(), cumulativeVideos, cumulativeViews);

        return VideoMonthlyStatsResponse.builder()
                .monthlyData(completeMonthlyData)
                .overallStats(overallStats)
                .totalSubscribers(subscriberCount)
                .totalMonths(completeMonthlyData.size())
                .startDate(firstVideoDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .endDate(lastVideoDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build();
    }

    /**
     * Calculate overall stats (reuse existing logic)
     */
    private Stats calculateOverallStats(String profileId) {
        List<Video> videos = videoRepository.findAllByProfileId(profileId);

        long totalViews = videos.stream().mapToLong(Video::getViewCount).sum();
        long totalLikes = videos.stream().mapToLong(Video::getLikeCount).sum();
        long totalDislikes = videos.stream().mapToLong(Video::getDislikeCount).sum();
        long totalComments = videos.stream().mapToLong(Video::getCommentCount).sum();

        return Stats.builder()
                .totalViews(totalViews)
                .totalLikes(totalLikes)
                .totalDislikes(totalDislikes)
                .totalComments(totalComments)
                .totalVideos((long) videos.size())
                .build();
    }

    // Thêm method này vào VideoService.java

    public GrowthDataResponse getGrowthData(
            String timeRange,
            String comparisonType,
            String customStartDate,
            String customEndDate) {

        log.info("=== Calculating Growth Data - TimeRange: {}, Comparison: {} ===",
                timeRange, comparisonType);

        // Calculate date ranges
        LocalDateTime currentEnd = LocalDateTime.now();
        LocalDateTime currentStart;
        LocalDateTime comparisonStart;
        LocalDateTime comparisonEnd;

        switch (timeRange.toLowerCase()) {
            case "week":
                currentStart = currentEnd.minusDays(7);
                comparisonStart = currentStart.minusDays(7);
                comparisonEnd = currentStart;
                break;
            case "month":
                currentStart = currentEnd.minusDays(30);
                comparisonStart = currentStart.minusDays(30);
                comparisonEnd = currentStart;
                break;
            case "year":
                currentStart = currentEnd.minusMonths(12);
                comparisonStart = currentStart.minusMonths(12);
                comparisonEnd = currentStart;
                break;
            case "custom":
                if (customStartDate != null && customEndDate != null) {
                    currentStart = LocalDate.parse(customStartDate).atStartOfDay();
                    currentEnd = LocalDate.parse(customEndDate).atTime(23, 59, 59);
                    long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                            currentStart, currentEnd);
                    comparisonStart = currentStart.minusDays(daysBetween);
                    comparisonEnd = currentStart;
                } else {
                    currentStart = currentEnd.minusDays(30);
                    comparisonStart = currentStart.minusDays(30);
                    comparisonEnd = currentStart;
                }
                break;
            default:
                currentStart = currentEnd.minusDays(7);
                comparisonStart = currentStart.minusDays(7);
                comparisonEnd = currentStart;
        }

        log.info("Current period: {} to {}", currentStart, currentEnd);
        log.info("Comparison period: {} to {}", comparisonStart, comparisonEnd);

        // Get current period data
        List<GrowthDataPoint> currentPeriodData = getGrowthDataPoints(
                currentStart, currentEnd, timeRange);

        // Get comparison period data
        List<GrowthDataPoint> comparisonPeriodData = getGrowthDataPoints(
                comparisonStart, comparisonEnd, timeRange);

        // Calculate summary
        GrowthSummary summary = calculateGrowthSummary(
                currentPeriodData, comparisonPeriodData,
                currentStart, currentEnd, comparisonStart, comparisonEnd);

        return GrowthDataResponse.builder()
                .currentPeriod(currentPeriodData)
                .comparisonPeriod(comparisonPeriodData)
                .summary(summary)
                .timeRange(timeRange)
                .comparisonType(comparisonType)
                .build();
    }

    private List<GrowthDataPoint> getGrowthDataPoints(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String timeRange) {

        List<GrowthDataPoint> dataPoints = new ArrayList<>();
        DateTimeFormatter formatter;

        if (timeRange.equals("week")) {
            // Daily data for week view
            formatter = DateTimeFormatter.ofPattern("EEE"); // Mon, Tue, Wed
            List<Object[]> videoStats = videoRepository.getDailyStats(startDate, endDate);

            LocalDate startLocalDate = startDate.toLocalDate();
            LocalDate endLocalDate = endDate.toLocalDate();
            List<Object[]> userStats = profileClient.getDailyUserRegistrations(
                    startLocalDate, endLocalDate).getResult();

            // Create map for video stats
            Map<LocalDate, Object[]> videoStatsMap = videoStats.stream()
                    .collect(Collectors.toMap(
                            row -> ((java.sql.Date) row[0]).toLocalDate(),
                            row -> row
                    ));

            // Create map for user stats - FIX HERE
            Map<LocalDate, Object[]> userStatsMap = new HashMap<>();
            if (userStats != null) {
                for (Object[] row : userStats) {
                    LocalDate date;
                    if (row[0] instanceof LocalDate) {
                        date = (LocalDate) row[0];
                    } else if (row[0] instanceof java.sql.Date) {
                        date = ((java.sql.Date) row[0]).toLocalDate();
                    } else if (row[0] instanceof String) {
                        date = LocalDate.parse((String) row[0]);
                    } else {
                        continue; // Skip invalid data
                    }
                    userStatsMap.put(date, row);
                }
            }

            // Fill in all days
            LocalDate current = startLocalDate;
            while (!current.isAfter(endLocalDate)) {
                Object[] vStats = videoStatsMap.get(current);
                Object[] uStats = userStatsMap.get(current);

                Long newVideos = vStats != null ? ((Number) vStats[1]).longValue() : 0L;
                Long totalViews = vStats != null ? ((Number) vStats[2]).longValue() : 0L;
                Long totalLikes = vStats != null ? ((Number) vStats[3]).longValue() : 0L;
                Long totalComments = vStats != null ? ((Number) vStats[4]).longValue() : 0L;
                Long newUsers = uStats != null ? ((Number) uStats[1]).longValue() : 0L;

                dataPoints.add(GrowthDataPoint.builder()
                        .period(current.format(formatter))
                        .date(current.toString())
                        .newUsers(newUsers)
                        .activeUsers(newUsers)
                        .newVideos(newVideos)
                        .totalViews(totalViews)
                        .totalLikes(totalLikes)
                        .totalComments(totalComments)
                        .engagementRate(calculateEngagementRate(totalViews, totalLikes, totalComments))
                        .build());

                current = current.plusDays(1);
            }

        } else if (timeRange.equals("month")) {
            // Daily data for month view
            formatter = DateTimeFormatter.ofPattern("MMM dd");
            List<Object[]> videoStats = videoRepository.getDailyStats(startDate, endDate);

            LocalDate startLocalDate = startDate.toLocalDate();
            LocalDate endLocalDate = endDate.toLocalDate();
            List<Object[]> userStats = profileClient.getDailyUserRegistrations(
                    startLocalDate, endLocalDate).getResult();

            Map<LocalDate, Object[]> videoStatsMap = videoStats.stream()
                    .collect(Collectors.toMap(
                            row -> ((java.sql.Date) row[0]).toLocalDate(),
                            row -> row
                    ));

            // FIX HERE - same as above
            Map<LocalDate, Object[]> userStatsMap = new HashMap<>();
            if (userStats != null) {
                for (Object[] row : userStats) {
                    LocalDate date;
                    if (row[0] instanceof LocalDate) {
                        date = (LocalDate) row[0];
                    } else if (row[0] instanceof java.sql.Date) {
                        date = ((java.sql.Date) row[0]).toLocalDate();
                    } else if (row[0] instanceof String) {
                        date = LocalDate.parse((String) row[0]);
                    } else {
                        continue;
                    }
                    userStatsMap.put(date, row);
                }
            }

            LocalDate current = startLocalDate;
            while (!current.isAfter(endLocalDate)) {
                Object[] vStats = videoStatsMap.get(current);
                Object[] uStats = userStatsMap.get(current);

                Long newVideos = vStats != null ? ((Number) vStats[1]).longValue() : 0L;
                Long totalViews = vStats != null ? ((Number) vStats[2]).longValue() : 0L;
                Long totalLikes = vStats != null ? ((Number) vStats[3]).longValue() : 0L;
                Long totalComments = vStats != null ? ((Number) vStats[4]).longValue() : 0L;
                Long newUsers = uStats != null ? ((Number) uStats[1]).longValue() : 0L;

                dataPoints.add(GrowthDataPoint.builder()
                        .period(current.format(formatter))
                        .date(current.toString())
                        .newUsers(newUsers)
                        .activeUsers(newUsers)
                        .newVideos(newVideos)
                        .totalViews(totalViews)
                        .totalLikes(totalLikes)
                        .totalComments(totalComments)
                        .engagementRate(calculateEngagementRate(totalViews, totalLikes, totalComments))
                        .build());

                current = current.plusDays(1);
            }

        } else { // year or custom
            // Monthly data
            formatter = DateTimeFormatter.ofPattern("MMM yyyy");
            List<Object[]> videoStats = videoRepository.getMonthlyStats(startDate, endDate);

            LocalDate startLocalDate = startDate.toLocalDate();
            LocalDate endLocalDate = endDate.toLocalDate();
            List<Object[]> userStats = profileClient.getMonthlyUserRegistrations(
                    startLocalDate, endLocalDate).getResult();

            Map<String, Object[]> videoStatsMap = videoStats.stream()
                    .collect(Collectors.toMap(
                            row -> row[0] + "-" + String.format("%02d", row[1]),
                            row -> row
                    ));

            // FIX HERE - for monthly data
            Map<String, Object[]> userStatsMap = new HashMap<>();
            if (userStats != null) {
                for (Object[] row : userStats) {
                    String key;
                    // row[0] = year, row[1] = month
                    if (row[0] instanceof Number && row[1] instanceof Number) {
                        key = row[0] + "-" + String.format("%02d", ((Number) row[1]).intValue());
                    } else {
                        continue;
                    }
                    userStatsMap.put(key, row);
                }
            }

            YearMonth current = YearMonth.from(startDate);
            YearMonth end = YearMonth.from(endDate);

            while (!current.isAfter(end)) {
                String key = current.getYear() + "-" + String.format("%02d", current.getMonthValue());
                Object[] vStats = videoStatsMap.get(key);
                Object[] uStats = userStatsMap.get(key);

                Long newVideos = vStats != null ? ((Number) vStats[2]).longValue() : 0L;
                Long totalViews = vStats != null ? ((Number) vStats[3]).longValue() : 0L;
                Long totalLikes = vStats != null ? ((Number) vStats[4]).longValue() : 0L;
                Long totalComments = vStats != null ? ((Number) vStats[5]).longValue() : 0L;
                Long newUsers = uStats != null ? ((Number) uStats[2]).longValue() : 0L;

                dataPoints.add(GrowthDataPoint.builder()
                        .period(current.format(formatter))
                        .date(current.atDay(1).toString())
                        .newUsers(newUsers)
                        .activeUsers(newUsers)
                        .newVideos(newVideos)
                        .totalViews(totalViews)
                        .totalLikes(totalLikes)
                        .totalComments(totalComments)
                        .engagementRate(calculateEngagementRate(totalViews, totalLikes, totalComments))
                        .build());

                current = current.plusMonths(1);
            }
        }

        return dataPoints;
    }

    private GrowthSummary calculateGrowthSummary(
            List<GrowthDataPoint> currentData,
            List<GrowthDataPoint> comparisonData,
            LocalDateTime currentStart,
            LocalDateTime currentEnd,
            LocalDateTime comparisonStart,
            LocalDateTime comparisonEnd) {

        // Sum up current period
        Long totalNewUsers = currentData.stream()
                .mapToLong(d -> d.getNewUsers() != null ? d.getNewUsers() : 0L).sum();
        Long totalActiveUsers = currentData.stream()
                .mapToLong(d -> d.getActiveUsers() != null ? d.getActiveUsers() : 0L).sum();
        Long totalNewVideos = currentData.stream()
                .mapToLong(d -> d.getNewVideos() != null ? d.getNewVideos() : 0L).sum();
        Long totalViews = currentData.stream()
                .mapToLong(d -> d.getTotalViews() != null ? d.getTotalViews() : 0L).sum();
        Long totalLikes = currentData.stream()
                .mapToLong(d -> d.getTotalLikes() != null ? d.getTotalLikes() : 0L).sum();
        Long totalComments = currentData.stream()
                .mapToLong(d -> d.getTotalComments() != null ? d.getTotalComments() : 0L).sum();

        // Sum up comparison period
        Long compNewUsers = comparisonData.stream()
                .mapToLong(d -> d.getNewUsers() != null ? d.getNewUsers() : 0L).sum();
        Long compNewVideos = comparisonData.stream()
                .mapToLong(d -> d.getNewVideos() != null ? d.getNewVideos() : 0L).sum();
        Long compViews = comparisonData.stream()
                .mapToLong(d -> d.getTotalViews() != null ? d.getTotalViews() : 0L).sum();
        Long compEngagement = comparisonData.stream()
                .mapToLong(d -> (d.getTotalLikes() != null ? d.getTotalLikes() : 0L) +
                        (d.getTotalComments() != null ? d.getTotalComments() : 0L)).sum();

        Long currentEngagement = totalLikes + totalComments;

        // Calculate growth rates
        Double userGrowthRate = calculateGrowthRate(totalNewUsers, compNewUsers);
        Double videoGrowthRate = calculateGrowthRate(totalNewVideos, compNewVideos);
        Double viewGrowthRate = calculateGrowthRate(totalViews, compViews);
        Double engagementGrowthRate = calculateGrowthRate(currentEngagement, compEngagement);

        return GrowthSummary.builder()
                .totalNewUsers(totalNewUsers)
                .totalActiveUsers(totalActiveUsers)
                .totalNewVideos(totalNewVideos)
                .totalViews(totalViews)
                .totalLikes(totalLikes)
                .totalComments(totalComments)
                .userGrowthRate(userGrowthRate)
                .videoGrowthRate(videoGrowthRate)
                .viewGrowthRate(viewGrowthRate)
                .engagementGrowthRate(engagementGrowthRate)
                .startDate(currentStart.toLocalDate().toString())
                .endDate(currentEnd.toLocalDate().toString())
                .comparisonStartDate(comparisonStart.toLocalDate().toString())
                .comparisonEndDate(comparisonEnd.toLocalDate().toString())
                .build();
    }

    private Double calculateGrowthRate(Long current, Long previous) {
        if (previous == null || previous == 0) {
            return current != null && current > 0 ? 100.0 : 0.0;
        }
        if (current == null) {
            return -100.0;
        }
        return ((current - previous) * 100.0) / previous;
    }

    private Double calculateEngagementRate(Long views, Long likes, Long comments) {
        if (views == null || views == 0) {
            return 0.0;
        }
        long engagement = (likes != null ? likes : 0L) + (comments != null ? comments : 0L);
        return Math.round((engagement * 100.0 / views) * 100.0) / 100.0;
    }
}