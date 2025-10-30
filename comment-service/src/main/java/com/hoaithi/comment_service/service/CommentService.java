package com.hoaithi.comment_service.service;

import com.hoaithi.comment_service.dto.request.CommentRequest;
import com.hoaithi.comment_service.dto.response.CommentCountResponse;
import com.hoaithi.comment_service.dto.response.CommentResponse;
import com.hoaithi.comment_service.dto.response.ProfileResponse;
import com.hoaithi.comment_service.entity.Comment;
import com.hoaithi.comment_service.entity.CommentHeart;
import com.hoaithi.comment_service.entity.Owner;
import com.hoaithi.comment_service.exception.AppException;
import com.hoaithi.comment_service.exception.ErrorCode;
import com.hoaithi.comment_service.mapper.CommentMapper;
import com.hoaithi.comment_service.repository.CommentHeartRepository;
import com.hoaithi.comment_service.repository.CommentRepository;
import com.hoaithi.comment_service.repository.httpclient.PostClient;
import com.hoaithi.comment_service.repository.httpclient.ProfileClient;
import com.hoaithi.comment_service.repository.httpclient.VideoClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CommentService {
    CommentRepository commentRepository;
    CommentHeartRepository commentHeartRepository;
    CommentMapper commentMapper;
    ProfileClient profileClient;
    VideoClient videoClient;
    PostClient postClient;
    public CommentResponse createComment(CommentRequest request){
        ProfileResponse response = profileClient.getMyProfile().getResult();

        Comment comment = commentMapper.toComment(request);
        comment.setOwner(Owner.builder()
                        .avatarUrl(response.getAvatarUrl())
                        .fullName(response.getFullName())
                        .profileId(response.getId())
                .build());
        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toCreationCommentResponse(savedComment);
    }

    public List<CommentResponse> getComments(String itemId){
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Comment> comments = commentRepository.findByItemId(itemId);

        List<String> commentIds = comments.stream().map(Comment::getId).toList();

        List<String> heartedCommentIds = commentHeartRepository
                .findByCommentIdInAndProfileId(commentIds, currentUserId)
                .stream()
                .map(CommentHeart::getCommentId)
                .toList();

        List<CommentResponse> commentResponses = new ArrayList<>();
        for(Comment comment: comments){
            CommentResponse response = commentMapper.toCreationCommentResponse(comment);
            response.setHearted(heartedCommentIds.contains(comment.getId()));
            commentResponses.add(response);
        }
        return commentResponses;
    }

    public CommentResponse updateComment(String commentId, CommentRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));
        if(!comment.getOwner().getProfileId().equals(authentication.getName())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        comment.setContent(request.getContent());
        return commentMapper.toCreationCommentResponse(comment);
    }

    public void deleteComment(String commentId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication.getName();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));
        if(canDeleteComment(comment, currentUserId)){
            commentRepository.deleteById(commentId);
        }else {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }
    private boolean canDeleteComment(Comment comment, String currentUserId){
        if(currentUserId.equals(comment.getOwner().getProfileId())) return true;

        return switch (comment.getCommentType()) {
            case POST -> {
                String postOwner = postClient.getOwnerById(comment.getItemId()).getOwnerId();
                yield postOwner.equals(currentUserId);
            }
            case VIDEO -> {
                String videoOwnerId = videoClient.getOwnerById(comment.getItemId()).getOwnerId();
                yield videoOwnerId.equals(currentUserId);
            }
            default -> false;
        };

    }

    public CommentResponse heartComment(String commentId){

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean alreadyHearted = commentHeartRepository.existsByCommentIdAndProfileId(commentId, userId);
        if(alreadyHearted){
            commentHeartRepository.deleteByCommentIdAndProfileId(commentId, userId);
        }else {
            commentHeartRepository.save(CommentHeart.builder()
                    .commentId(commentId)
                    .profileId(userId)
                    .build());
        }
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));
        comment.setHeartCount(commentHeartRepository.countByCommentId(commentId));
        Comment savedComment = commentRepository.save(comment);
        CommentResponse response = commentMapper.toCreationCommentResponse(savedComment);
        response.setHearted(!alreadyHearted);
        return response;
    }
    public CommentCountResponse getCommentCount(String itemId){
        return CommentCountResponse.builder()
                .commentCount(commentRepository.countByItemId(itemId))
                .build();
    }


}
