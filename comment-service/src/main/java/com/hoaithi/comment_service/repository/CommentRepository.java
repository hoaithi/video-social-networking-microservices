package com.hoaithi.comment_service.repository;

import com.hoaithi.comment_service.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> findByItemId(String itemId);

    Long countByItemId(String itemId);

    List<Comment> findByParentCommentIdOrderByCreatedAtAsc(String parentComment_id);
}
