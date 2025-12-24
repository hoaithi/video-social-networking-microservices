package com.hoaithi.comment_service.repository;

import com.hoaithi.comment_service.entity.CommentHeart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentHeartRepository extends JpaRepository<CommentHeart, String> {
    boolean existsByCommentIdAndProfileId(String commentId, String profileId);
    void deleteByCommentIdAndProfileId(String commentId, String profileId);
    Long countByCommentId(String commentId);
    List<CommentHeart> findByCommentIdInAndProfileId(List<String> commentIds, String profileId);
}
