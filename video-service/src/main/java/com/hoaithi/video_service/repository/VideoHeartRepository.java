package com.hoaithi.video_service.repository;


import com.hoaithi.video_service.entity.VideoHeart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoHeartRepository extends JpaRepository<VideoHeart, Long> {
    List<VideoHeart> findByProfileId(String profileId);
    Optional<VideoHeart> findByProfileIdAndVideoId(String profileId, String videoId);

    boolean existsByProfileIdAndVideoId(String currentProfileId, String videoId);

    void deleteByProfileIdAndVideoId(String currentProfileId, String videoId);

    Long countByVideoId(String videoId);
}
