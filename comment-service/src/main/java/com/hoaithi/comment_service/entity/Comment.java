package com.hoaithi.comment_service.entity;
import com.hoaithi.comment_service.enums.CommentType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "comments")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String itemId;
    String itemType;
    CommentType commentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Owner owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> replies = new ArrayList<>();


    String content;
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    LocalDateTime updatedAt = LocalDateTime.now();
    @Builder.Default
    Long heartCount = 0L;

}


