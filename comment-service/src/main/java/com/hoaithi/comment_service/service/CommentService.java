package com.hoaithi.comment_service.service;

import com.hoaithi.comment_service.dto.request.CommentRequest;
import com.hoaithi.comment_service.dto.response.CommentCountResponse;
import com.hoaithi.comment_service.dto.response.CommentResponse;
import com.hoaithi.comment_service.dto.response.OwnerResponse;
import com.hoaithi.comment_service.dto.response.ProfileResponse;
import com.hoaithi.comment_service.entity.Comment;
import com.hoaithi.comment_service.entity.CommentHeart;
import com.hoaithi.comment_service.entity.Owner;
import com.hoaithi.comment_service.enums.CommentType;
import com.hoaithi.comment_service.exception.AppException;
import com.hoaithi.comment_service.exception.ErrorCode;
import com.hoaithi.comment_service.mapper.CommentMapper;
import com.hoaithi.comment_service.repository.CommentHeartRepository;
import com.hoaithi.comment_service.repository.CommentRepository;
import com.hoaithi.comment_service.repository.OwnerRepository;
import com.hoaithi.comment_service.repository.httpclient.PostClient;
import com.hoaithi.comment_service.repository.httpclient.ProfileClient;
import com.hoaithi.comment_service.repository.httpclient.VideoClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CommentService {
    CommentRepository commentRepository;
    CommentHeartRepository commentHeartRepository;
    CommentMapper commentMapper;
    OwnerRepository ownerRepository;
    ProfileClient profileClient;
    VideoClient videoClient;
    PostClient postClient;
    public CommentResponse createComment(CommentRequest request){


        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        Comment comment = Comment.builder()
                .itemId(request.getItemId())
                .commentType(CommentType.valueOf(request.getCommentType()))
                .content(request.getContent())
                .build();
        Owner owner = ownerRepository.findById(currentUserId)
                .orElseGet(() -> {
                    ProfileResponse response =
                            profileClient.getMyProfile().getResult();

                    Owner newOwner = Owner.builder()
                            .profileId(response.getId())
                            .fullName(response.getFullName())
                            .avatarUrl(response.getAvatarUrl())
                            .build();

                    return ownerRepository.save(newOwner);
                });

        owner = ownerRepository.save(owner);
        comment.setOwner(owner);
        if(request.getParentCommentId()!=null&&!request.getParentCommentId().isEmpty()){
            Comment parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));
            comment.setParentComment(parentComment);
        }
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
    @Transactional(readOnly = true)
    public List<CommentResponse> getRepliesByCommentId(String commentId) {
        List<Comment> replies = commentRepository.findByParentCommentIdOrderByCreatedAtAsc(commentId);

        return replies.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private CommentResponse mapToDto(Comment comment) {
        String currentUserId =
                SecurityContextHolder.getContext().getAuthentication().getName();

        boolean isHearted = commentHeartRepository
                .existsByCommentIdAndProfileId(comment.getId(), currentUserId);
        OwnerResponse owner = OwnerResponse.builder()
                .profileId(comment.getOwner().getProfileId())
                .fullName(comment.getOwner().getFullName())
                .avatarUrl(comment.getOwner().getAvatarUrl())
                .build();
        return CommentResponse.builder()
                .id(comment.getId())
                .itemId(comment.getItemId())
                .parentCommentId(
                        comment.getParentComment() != null
                                ? comment.getParentComment().getId()
                                : null
                )
                .commentType(comment.getCommentType().name())
                .owner(owner)
                .content(comment.getContent())
                .hearted(isHearted)
                .heartCount(comment.getHeartCount())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replies(List.of()) // ⭐ luôn rỗng
                .build();
    }



}
