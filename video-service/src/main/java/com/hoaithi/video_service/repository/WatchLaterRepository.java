package com.hoaithi.video_service.repository;

import com.hoaithi.video_service.entity.Video;
import com.hoaithi.video_service.entity.VideoWatchLater;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WatchLaterRepository extends JpaRepository<VideoWatchLater, Long> {
    @Query("SELECT vwl.video FROM VideoWatchLater vwl WHERE vwl.profileId = :profileId")
    Page<Video> findAllVideosByProfileId(@Param("profileId") String profileId, Pageable pageable);

    boolean existsByVideoIdAndProfileId(String videoId, String profileId);

    void deleteByVideoIdAndProfileId(String videoId, String currentUserId);
}
