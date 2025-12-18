package com.hoaithi.profile_service.repository;

import com.hoaithi.profile_service.entity.Profile;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
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

    // Get daily user registrations
    @Query("SELECT p.createdAt as date, COUNT(p) as newUsers " +
            "FROM profiles p " +
            "WHERE p.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY p.createdAt " +
            "ORDER BY p.createdAt")
    List<Object[]> getDailyUserRegistrations(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Get monthly user registrations
    @Query("SELECT YEAR(p.createdAt) as year, MONTH(p.createdAt) as month, " +
            "COUNT(p) as newUsers " +
            "FROM profiles p " +
            "WHERE p.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY YEAR(p.createdAt), MONTH(p.createdAt) " +
            "ORDER BY YEAR(p.createdAt), MONTH(p.createdAt)")
    List<Object[]> getMonthlyUserRegistrations(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Get total users for a period
    @Query("SELECT COUNT(p) FROM profiles p " +
            "WHERE p.createdAt BETWEEN :startDate AND :endDate")
    Long countUsersInPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Get users created before a specific date
    @Query("SELECT COUNT(p) FROM profiles p " +
            "WHERE p.createdAt <= :endDate")
    Long countUsersUpToDate(@Param("endDate") LocalDate endDate);
}
