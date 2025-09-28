package com.hoaithi.post_service.repository;

import com.hoaithi.post_service.entity.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    /**
     * Finds posts by profile ID.
     *
     * @param profileId the profile ID
     * @return a list of posts associated with the profile ID
     */
    List<Post> findByProfileId(String profileId);
}
