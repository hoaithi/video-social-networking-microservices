package com.hoaithi.comment_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;


import java.time.Instant;


@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(
        name = "comment_hearts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id", "profile_id"})
)
public class CommentHeart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String commentId;
    String profileId;

    @Builder.Default
    Instant createdAt = Instant.now();

}
