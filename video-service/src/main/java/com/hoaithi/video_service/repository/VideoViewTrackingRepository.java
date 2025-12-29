package com.hoaithi.video_service.repository;

import com.hoaithi.video_service.entity.VideoViewTracking;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoViewTrackingRepository extends JpaRepository<VideoViewTracking, String> {

    Optional<VideoViewTracking> findByVideoIdAndSessionId(String videoId, String sessionId);

    List<VideoViewTracking> findByVideoId(String videoId);

    List<VideoViewTracking> findByProfileId(String profileId);

    @Query("SELECT COUNT(vt) FROM VideoViewTracking vt WHERE vt.videoId = :videoId AND vt.isValidView = true")
    Long countValidViewsByVideoId(@Param("videoId") String videoId);

    @Query("SELECT COUNT(vt) FROM VideoViewTracking vt " +
            "WHERE vt.videoId = :videoId " +
            "AND vt.isValidView = true " +
            "AND vt.completedAt BETWEEN :startDate AND :endDate")
    Long countValidViewsByVideoIdAndDateRange(
            @Param("videoId") String videoId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT SUM(vt.watchDuration) FROM VideoViewTracking vt WHERE vt.videoId = :videoId")
    Double getTotalWatchDurationByVideoId(@Param("videoId") String videoId);

    @Query("SELECT AVG(vt.watchPercentage) FROM VideoViewTracking vt " +
            "WHERE vt.videoId = :videoId AND vt.watchPercentage IS NOT NULL")
    Double getAverageWatchPercentageByVideoId(@Param("videoId") String videoId);

    @Query("SELECT COUNT(DISTINCT vt.profileId) FROM VideoViewTracking vt WHERE vt.videoId = :videoId")
    Long countUniqueViewersByVideoId(@Param("videoId") String videoId);

    boolean existsByVideoIdAndProfileId(String videoId, String profileId);

    @Query("SELECT vt FROM VideoViewTracking vt " +
            "WHERE vt.videoId = :videoId AND vt.profileId = :profileId " +
            "ORDER BY vt.startedAt DESC")
    List<VideoViewTracking> findRecentTrackingByVideoIdAndProfileId(
            @Param("videoId") String videoId,
            @Param("profileId") String profileId
    );

    @Query("DELETE FROM VideoViewTracking vt WHERE vt.startedAt < :cutoffDate AND vt.isValidView = false")
    void deleteOldInvalidTracking(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT DATE(vt.completedAt) as date, COUNT(vt) as validViews " +
            "FROM VideoViewTracking vt " +
            "WHERE vt.videoId = :videoId " +
            "AND vt.isValidView = true " +
            "AND vt.completedAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(vt.completedAt) " +
            "ORDER BY DATE(vt.completedAt)")
    List<Object[]> getDailyValidViewStats(
            @Param("videoId") String videoId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Upsert view progress - thread-safe, no race condition
     * PostgreSQL ON CONFLICT DO UPDATE
     */
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO video_view_tracking 
        (id, video_id, profile_id, session_id, watch_duration, watch_percentage, 
         video_duration, has_interacted, is_valid_view, started_at, last_updated,
         completed_at, ip_address, user_agent)
        VALUES (gen_random_uuid(), :videoId, :profileId, :sessionId, :watchDuration, 
                :watchPercentage, :videoDuration, false, false, NOW(), NOW(), NULL, NULL, NULL)
        ON CONFLICT (session_id) 
        DO UPDATE SET 
            watch_duration = EXCLUDED.watch_duration,
            watch_percentage = EXCLUDED.watch_percentage,
            last_updated = NOW()
        """, nativeQuery = true)
    void upsertViewProgress(
            @Param("videoId") String videoId,
            @Param("profileId") String profileId,
            @Param("sessionId") String sessionId,
            @Param("watchDuration") Double watchDuration,
            @Param("watchPercentage") Double watchPercentage,
            @Param("videoDuration") Double videoDuration
    );
}