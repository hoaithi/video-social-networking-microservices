package com.hoaithi.video_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "videos")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private double duration;

    private boolean isPremium;

    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;

    @Column(name = "video_url", nullable = false, length = 2000)
    private String videoUrl;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "like_count")
    @Builder.Default
    private Long likeCount = 0L;

    @Column(name = "dislike_count")
    @Builder.Default
    private Long dislikeCount = 0L;;

    @Column(name = "comment_count")
    @Builder.Default
    private Long commentCount = 0L;

    @Column(name = "profile_id")
    private String profileId;

    // Thêm vào Video.java
    @Column(name = "valid_view_count")
    @Builder.Default
    private Long validViewCount = 0L; // View thực sự hợp lệ

    @Column(name = "min_watch_duration")
    private Double minWatchDuration; // Thời gian tối thiểu cần xem (30s hoặc 30% duration)



    // relationships
    @OneToMany(
            mappedBy = "video",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<VideoPlaylist> videoPlaylists = new ArrayList<>();

    @OneToMany(
            mappedBy = "video",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<VideoHistory> videoHistories = new ArrayList<>();

    @OneToMany(
            mappedBy = "video",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<VideoReaction> videoReactions = new ArrayList<>();


}
