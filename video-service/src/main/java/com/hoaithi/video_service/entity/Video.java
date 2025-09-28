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

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "video_url", nullable = false)
    private String videoUrl;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "heart_count")
    @Builder.Default
    private Long heartCount = 0L;

    @Column(name = "comment_count")
    @Builder.Default
    private Long commentCount = 0L;

    @Column(name = "profile_id")
    private String profileId;



    // relationships
    @OneToMany(mappedBy = "video")
    private List<VideoPlaylist> videoPlaylists = new ArrayList<>();

    @OneToMany(mappedBy = "video")
    private List<VideoHistory> videoHistories = new ArrayList<>();

    @OneToMany(mappedBy = "video")
    private List<VideoHeart> videoHearts = new ArrayList<>();

}
