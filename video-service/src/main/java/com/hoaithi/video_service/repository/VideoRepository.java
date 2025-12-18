package com.hoaithi.video_service.repository;

import com.hoaithi.video_service.entity.Video;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {
    Page<Video> findAllByProfileId(String profileId, Pageable pageable);
    List<Video> findAllByProfileId(String profileId);
    Long countByProfileId(String profileId);

    // Search by title
    Page<Video> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Filter by isPremium
    Page<Video> findByIsPremium(Boolean isPremium, Pageable pageable);

    // Search by title AND filter by isPremium
    Page<Video> findByTitleContainingIgnoreCaseAndIsPremium(
            String title, Boolean isPremium, Pageable pageable);


    // NEW: Monthly video upload stats
    @Query("SELECT YEAR(v.publishedAt) as year, MONTH(v.publishedAt) as month, " +
            "COUNT(v) as videoCount " +
            "FROM Video v " +
            "WHERE v.profileId = :profileId " +
            "AND v.publishedAt IS NOT NULL " +
            "GROUP BY YEAR(v.publishedAt), MONTH(v.publishedAt) " +
            "ORDER BY YEAR(v.publishedAt), MONTH(v.publishedAt)")
    List<Object[]> getMonthlyVideoUploadsByProfileId(@Param("profileId") String profileId);

    // NEW: Monthly aggregated stats (views, likes, comments)
    @Query("SELECT YEAR(v.publishedAt) as year, MONTH(v.publishedAt) as month, " +
            "SUM(v.viewCount) as totalViews, " +
            "SUM(v.likeCount) as totalLikes, " +
            "SUM(v.dislikeCount) as totalDislikes, " +
            "SUM(v.commentCount) as totalComments " +
            "FROM Video v " +
            "WHERE v.profileId = :profileId " +
            "AND v.publishedAt IS NOT NULL " +
            "GROUP BY YEAR(v.publishedAt), MONTH(v.publishedAt) " +
            "ORDER BY YEAR(v.publishedAt), MONTH(v.publishedAt)")
    List<Object[]> getMonthlyEngagementByProfileId(@Param("profileId") String profileId);

    // NEW: Get first and last video dates
    @Query("SELECT MIN(v.publishedAt), MAX(v.publishedAt) FROM Video v " +
            "WHERE v.profileId = :profileId " +
            "AND v.publishedAt IS NOT NULL")
    Object[] getVideoDateRange(@Param("profileId") String profileId);

    // NEW: Get videos published in a specific month
    @Query("SELECT v FROM Video v " +
            "WHERE v.profileId = :profileId " +
            "AND YEAR(v.publishedAt) = :year " +
            "AND MONTH(v.publishedAt) = :month " +
            "ORDER BY v.publishedAt DESC")
    List<Video> findByProfileIdAndYearMonth(
            @Param("profileId") String profileId,
            @Param("year") Integer year,
            @Param("month") Integer month);

    // NEW: Count videos up to a specific date
    @Query("SELECT COUNT(v) FROM Video v " +
            "WHERE v.profileId = :profileId " +
            "AND v.publishedAt <= :endDate")
    Long countVideosUpToDate(
            @Param("profileId") String profileId,
            @Param("endDate") LocalDateTime endDate);

    // Get daily stats for a date range (for week view)
    @Query("SELECT DATE(v.publishedAt) as date, " +
            "COUNT(v) as newVideos, " +
            "SUM(v.viewCount) as totalViews, " +
            "SUM(v.likeCount) as totalLikes, " +
            "SUM(v.commentCount) as totalComments " +
            "FROM Video v " +
            "WHERE v.publishedAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(v.publishedAt) " +
            "ORDER BY DATE(v.publishedAt)")
    List<Object[]> getDailyStats(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Get monthly stats for a date range (for month/year view)
    @Query("SELECT YEAR(v.publishedAt) as year, MONTH(v.publishedAt) as month, " +
            "COUNT(v) as newVideos, " +
            "SUM(v.viewCount) as totalViews, " +
            "SUM(v.likeCount) as totalLikes, " +
            "SUM(v.commentCount) as totalComments " +
            "FROM Video v " +
            "WHERE v.publishedAt BETWEEN :startDate AND :endDate " +
            "GROUP BY YEAR(v.publishedAt), MONTH(v.publishedAt) " +
            "ORDER BY YEAR(v.publishedAt), MONTH(v.publishedAt)")
    List<Object[]> getMonthlyStats(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Get total stats for a date range
    @Query("SELECT COUNT(v) as totalVideos, " +
            "SUM(v.viewCount) as totalViews, " +
            "SUM(v.likeCount) as totalLikes, " +
            "SUM(v.commentCount) as totalComments " +
            "FROM Video v " +
            "WHERE v.publishedAt BETWEEN :startDate AND :endDate")
    Object[] getTotalStatsForPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

}
