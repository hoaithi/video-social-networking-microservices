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
    String parentCommentId;
    Owner owner;
    String content;
    boolean hearted;
    LocalDateTime createdDate;
    LocalDateTime updatedDate;
    Long heartCount;

}
