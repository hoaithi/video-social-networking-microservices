package com.hoaithi.profile_service.repository;

import com.hoaithi.profile_service.entity.Profile;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ProfileRepository extends JpaRepository<Profile, String> {
    Profile findByUserId(String userId);

    Optional<Profile> findByEmail(String email);

    // Search by fullName or email
    Page<Profile> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String fullName, String email, Pageable pageable);

    // Filter by hasPassword
    Page<Profile> findByHasPassword(Boolean hasPassword, Pageable pageable);

    // Search by fullName or email AND filter by hasPassword
    @Query("SELECT p FROM profiles p WHERE " +
            "(LOWER(p.fullName) LIKE LOWER(CONCAT('%', :fullName, '%')) OR " +
            "LOWER(p.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "p.hasPassword = :hasPassword")
    Page<Profile> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndHasPassword(
            @Param("fullName") String fullName,
            @Param("email") String email,
            @Param("hasPassword") Boolean hasPassword,
            Pageable pageable);
}
