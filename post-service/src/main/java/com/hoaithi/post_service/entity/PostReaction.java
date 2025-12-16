package com.hoaithi.post_service.entity;

import com.hoaithi.post_service.enums.ReactionType;
import lombok.*;

import java.time.LocalDateTime;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "post_reactions")
public class PostReaction {

    @Id
    private String id; // MongoDB ID luôn là String/ObjectId

    @Field("user_id")
    private String userId;  // thay vì @ManyToOne User

    @Field("post_id")
    private String postId;  // thay vì @ManyToOne Post

    @Field("reaction_type")
    private ReactionType reactionType;

    @Field("created_at")
    private LocalDateTime createdAt;
}
