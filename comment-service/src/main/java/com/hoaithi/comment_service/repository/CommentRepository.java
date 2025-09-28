package com.hoaithi.comment_service.repository;

import com.hoaithi.comment_service.entity.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByItemId(String itemId);

    Long countByItemId(String itemId);
}
