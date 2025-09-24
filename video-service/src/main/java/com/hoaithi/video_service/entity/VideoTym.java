package com.hoaithi.video_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "video_tym" , uniqueConstraints = {
        @UniqueConstraint(columnNames = {"video_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoTym {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @JoinColumn(name = "user_id", nullable = false)
    private String userId;
}