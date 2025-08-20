package com.hoaithi.post_service.repository;

import com.hoaithi.post_service.entity.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    /**
     * Finds posts by user ID.
     *
     * @param userId the user ID
     * @return a list of posts associated with the user ID
     */
    List<Post> findByUserId(String userId);
}
