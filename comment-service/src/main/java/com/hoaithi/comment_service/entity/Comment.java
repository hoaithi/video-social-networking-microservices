package com.hoaithi.comment_service.entity;

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
    String parentCommentId;
    Owner owner;
    String content;
    @Builder.Default
    LocalDateTime createdDate = LocalDateTime.now();
    @Builder.Default
    LocalDateTime updatedDate = LocalDateTime.now();
    @Builder.Default
    Long heartCount = 0L;
}
