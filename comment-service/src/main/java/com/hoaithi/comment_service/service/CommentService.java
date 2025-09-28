package com.hoaithi.comment_service.service;

import com.hoaithi.comment_service.dto.request.CommentRequest;
import com.hoaithi.comment_service.dto.response.CommentCountResponse;
import com.hoaithi.comment_service.dto.response.CommentResponse;
import com.hoaithi.comment_service.dto.response.ProfileResponse;
import com.hoaithi.comment_service.entity.Comment;
import com.hoaithi.comment_service.entity.CommentHeart;
import com.hoaithi.comment_service.exception.AppException;
import com.hoaithi.comment_service.exception.ErrorCode;
import com.hoaithi.comment_service.mapper.CommentMapper;
import com.hoaithi.comment_service.repository.CommentHeartRepository;
import com.hoaithi.comment_service.repository.CommentRepository;
import com.hoaithi.comment_service.repository.httpclient.ProfileClient;
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

    public CommentResponse createComment(CommentRequest request){
        ProfileResponse response = profileClient.getMyProfile().getResult();

        Comment comment = commentMapper.toComment(request);
        comment.setAvatarUrl(response.getAvatarUrl());
        comment.setFullName(response.getFullName());
        comment.setProfileId(response.getProfileId());
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
        if(!comment.getProfileId().equals(authentication.getName())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        comment.setContent(request.getContent());
        return commentMapper.toCreationCommentResponse(comment);
    }

    public void deleteComment(String commentId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));
        if(!comment.getProfileId().equals(authentication.getName())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        commentRepository.deleteById(commentId);
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
