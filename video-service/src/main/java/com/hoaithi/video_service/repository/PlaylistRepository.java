package com.hoaithi.video_service.repository;

import com.hoaithi.video_service.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, String> {
    List<Playlist> findAllByProfileId(String profileId);
}
