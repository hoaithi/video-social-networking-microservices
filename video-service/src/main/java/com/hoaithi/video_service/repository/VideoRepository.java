package com.hoaithi.video_service.repository;

import com.hoaithi.video_service.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
