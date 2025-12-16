package com.hoaithi.post_service.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "posts")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Post {
    @Id
    String id;
    String profileId;
    String profileImage;
    String profileName;
    String content;
    String title;
    String imageUrl;
    @Builder.Default
    Instant createdAt = Instant.now();
    @Builder.Default
    Long likeCount = 0L;
    @Builder.Default
    Long dislikeCount = 0L;;
    @Builder.Default
    Long commentCount = 0L;

}
