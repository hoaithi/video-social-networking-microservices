package com.hoaithi.video_service.repository;

import com.hoaithi.video_service.entity.VideoHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<VideoHistory, Long> {
    List<VideoHistory> findByUserId(String userId);
}
