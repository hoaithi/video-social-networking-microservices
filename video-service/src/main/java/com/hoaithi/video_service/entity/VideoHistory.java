package com.hoaithi.video_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_histories" , uniqueConstraints = {
        @UniqueConstraint(columnNames = {"video_id", "profile_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(name = "profile_id", nullable = false)
    private String profileId;

    @Column(name = "current_time_value")
    private double currentTime;

    private double duration;

    private double percentage;

    private LocalDateTime lastWatched; // Thời điểm cập nhật gần nhất

    private boolean isCompleted = false; // Đánh dấu đã xem hết hay chưa

    private LocalDateTime createdAt;

}
