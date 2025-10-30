package com.hoaithi.video_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_watch_laters" , uniqueConstraints = {
        @UniqueConstraint(columnNames = {"video_id", "profile_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoWatchLater {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(name = "profile_id", nullable = false)
    private String profileId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
