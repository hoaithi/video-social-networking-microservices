package com.hoaithi.video_service.repository;

import com.hoaithi.video_service.entity.Video;
import com.hoaithi.video_service.entity.VideoHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistoryRepository extends JpaRepository<VideoHistory, Long> {
    List<VideoHistory> findByProfileId(String profileId);
    boolean existsByProfileIdAndVideoId(String profileId, String videoId);

    Optional<VideoHistory> findByProfileIdAndVideoId(String profileId, String videoId);


    @Query("SELECT vh.video FROM VideoHistory vh WHERE vh.profileId = :profileId")
    Page<Video> findAllVideosByProfileId(@Param("profileId") String profileId, Pageable pageable);

    boolean existsByVideoIdAndProfileId(String videoId, String currentUserId);

    void deleteByVideoIdAndProfileId(String videoId, String currentUserId);

    void deleteAllByProfileId(String profileId);
}
