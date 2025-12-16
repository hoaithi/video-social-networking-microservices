package com.hoaithi.post_service.repository;

import com.hoaithi.post_service.entity.PostReaction;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostReactionRepository extends MongoRepository<PostReaction, String> {

    Optional<PostReaction> findByUserIdAndPostId(String userId, String postId);

    void deleteByPostId(String postId);
}
