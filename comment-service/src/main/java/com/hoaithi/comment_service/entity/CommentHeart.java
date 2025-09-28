package com.hoaithi.comment_service.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "comment_hearts")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CommentHeart {
    @Id
    String id;

    String commentId;
    String profileId;

    @Builder.Default
    Instant createdAt = Instant.now();

}
