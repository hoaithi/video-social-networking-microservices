package com.hoaithi.comment_service.dto.response;

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
    String parentCommentId;
    String itemId;
    String profileId;
    String fullName;
    String avatarUrl;
    String content;
    boolean hearted;
    LocalDateTime createdDate;
    LocalDateTime updatedDate;
    Long heartCount;

}
