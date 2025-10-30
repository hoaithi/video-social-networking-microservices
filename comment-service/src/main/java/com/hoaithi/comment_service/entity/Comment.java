package com.hoaithi.comment_service.entity;

import com.hoaithi.comment_service.enums.CommentType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "comments")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Comment {
    @Id
    String id;
    String itemId;
    String itemType;
    CommentType commentType;
    String parentCommentId;
    Owner owner;
    String content;
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    LocalDateTime updatedAt = LocalDateTime.now();
    @Builder.Default
    Long heartCount = 0L;
}
