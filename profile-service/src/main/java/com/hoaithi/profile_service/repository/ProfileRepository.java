package com.hoaithi.profile_service.repository;

import com.hoaithi.profile_service.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ProfileRepository extends JpaRepository<Profile, String> {
    Profile findByUserId(String userId);

    Optional<Profile> findByEmail(String email);
}
