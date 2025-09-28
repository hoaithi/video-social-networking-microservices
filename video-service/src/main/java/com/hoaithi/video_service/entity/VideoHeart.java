package com.hoaithi.video_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "video_hearts" , uniqueConstraints = {
        @UniqueConstraint(columnNames = {"video_id", "profile_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoHeart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @JoinColumn(name = "profile_id", nullable = false)
    private String profileId;
}