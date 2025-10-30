package com.hoaithi.video_service.repository;

import com.hoaithi.video_service.entity.VideoReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoReactionRepository extends JpaRepository<VideoReaction, Long> {
    Optional<VideoReaction> findByProfileIdAndVideoId(String profileId, String videoId);
}
