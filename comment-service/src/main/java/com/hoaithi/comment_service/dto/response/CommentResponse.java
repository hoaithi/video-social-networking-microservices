package com.hoaithi.comment_service.dto.response;

import com.hoaithi.comment_service.entity.Owner;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse {
    String id;
    String itemId;
    String parentCommentId;
    String commentType;
    Owner owner;
    String content;
    boolean hearted;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    Long heartCount;
}
