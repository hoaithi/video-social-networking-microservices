package com.hoaithi.video_service.repository;


import com.hoaithi.video_service.entity.VideoTym;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TymRepository extends JpaRepository<VideoTym, Long> {
    List<VideoTym> findByUserId(String userId);
    Optional<VideoTym> findByUserIdAndVideoId(String userId, String videoId);
}
