package com.hoaithi.video_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "video_playlists" , uniqueConstraints = {
        @UniqueConstraint(columnNames = {"video_id", "playlist_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoPlaylist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;
}
