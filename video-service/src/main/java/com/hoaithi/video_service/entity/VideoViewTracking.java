package com.hoaithi.video_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_view_tracking", indexes = {
        @Index(name = "idx_video_session", columnList = "video_id, session_id"),
        @Index(name = "idx_profile_video", columnList = "profile_id, video_id"),
        @Index(name = "idx_started_at", columnList = "started_at")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoViewTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "video_id", nullable = false)
    private String videoId;

    @Column(name = "profile_id")
    private String profileId;

    @Column(name = "session_id", nullable = false, unique = true)
    private String sessionId;

    @Column(name = "watch_duration")
    private Double watchDuration;

    @Column(name = "video_duration")
    private Double videoDuration;

    @Column(name = "watch_percentage")
    private Double watchPercentage;

    @Column(name = "has_interacted")
    @Builder.Default
    private Boolean hasInteracted = false;

    @Column(name = "is_valid_view")
    @Builder.Default
    private Boolean isValidView = false;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
}