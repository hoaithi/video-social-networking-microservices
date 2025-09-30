package com.hoaithi.video_service.repository;

import com.hoaithi.video_service.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {
    Page<Video> findAllByProfileId(String profileId, Pageable pageable);
}
