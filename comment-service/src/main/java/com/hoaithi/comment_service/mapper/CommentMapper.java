package com.hoaithi.comment_service.mapper;

import com.hoaithi.comment_service.dto.request.CommentRequest;
import com.hoaithi.comment_service.dto.response.CommentResponse;
import com.hoaithi.comment_service.entity.Comment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    Comment toComment(CommentRequest request);
    CommentResponse toCreationCommentResponse(Comment comment);
}
