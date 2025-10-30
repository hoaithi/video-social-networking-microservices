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
    String content;
    String title;
    String imageUrl;
    @Builder.Default
    Instant createdAt = Instant.now();
    @Builder.Default
    Long heartCount = 0L;
}
