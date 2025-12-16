package com.hoaithi.profile_service.repository;

import com.hoaithi.profile_service.entity.Membership;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    /**
     * Find all memberships for a user
     * @param userId The user ID
     * @return List of memberships
     */
    List<Membership> findByUserId(String userId);

    /**
     * Find all memberships for a channel
     * @param channelId The channel's user ID
     * @return List of memberships
     */
    @Query("SELECT m FROM Membership m WHERE m.membershipTier.user.id = :channelId")
    List<Membership> findByChannelId(String channelId);

    /**
     * Find active membership for a user and channel
     * @param userId The user ID
     * @param channelId The channel's user ID
     * @return Optional of Membership
     */
    @Query("SELECT m FROM Membership m WHERE m.user.id = :userId AND m.membershipTier.user.id = :channelId AND m.isActive = true")
    Optional<Membership> findActiveByUserIdAndChannelId(String userId, String channelId);

    /**
     * Check if user has active membership for a channel
     * @param userId The user ID
     * @param channelId The channel's user ID
     * @param isActive Whether membership is active
     * @return True if active membership exists
     */
    @Query("SELECT COUNT(m) > 0 FROM Membership m WHERE m.user.id = :userId AND m.membershipTier.user.id = :channelId AND m.isActive = :isActive")
    boolean existsByUserIdAndChannelIdAndIsActive(String userId, String channelId, boolean isActive);

    /**
     * Count active memberships for a membership tier
     * @param membershipTierId The membership tier ID
     * @param isActive Whether membership is active
     * @return Count of active memberships
     */
    long countByMembershipTierIdAndIsActive(Long membershipTierId, boolean isActive);

    // New count methods - Fixed to use correct field path
    @Query("SELECT COUNT(m) FROM Membership m WHERE m.user.id = :profileId")
    Long countByProfileId(@Param("profileId") String profileId);

    // Count distinct users who have memberships to this channel
    @Query("SELECT COUNT(DISTINCT m.user.id) FROM Membership m WHERE m.membershipTier.user.id = :profileId")
    Long countMembersByChannelId(@Param("profileId") String profileId);

    @Query("SELECT COUNT(m) FROM Membership m WHERE m.user.id = :profileId AND m.isActive = true")
    Long countActiveByProfileId(@Param("profileId") String profileId);

    // NEW: Monthly member growth for channel
    @Query("SELECT YEAR(m.startDate) as year, MONTH(m.startDate) as month, " +
            "COUNT(DISTINCT m.user.id) as newMembers " +
            "FROM Membership m " +
            "WHERE m.membershipTier.user.id = :profileId " +
            "GROUP BY YEAR(m.startDate), MONTH(m.startDate) " +
            "ORDER BY YEAR(m.startDate), MONTH(m.startDate)")
    List<Object[]> getMonthlyNewMembersByChannelId(@Param("profileId") String profileId);

    // Get memberships created within date range
    @Query("SELECT m FROM Membership m " +
            "WHERE m.membershipTier.user.id = :profileId " +
            "AND m.startDate BETWEEN :startDate AND :endDate " +
            "ORDER BY m.startDate ASC")
    List<Membership> findByChannelIdAndDateRange(
            @Param("profileId") String profileId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Count distinct members up to a specific date (for cumulative count)
    @Query("SELECT COUNT(DISTINCT m.user.id) FROM Membership m " +
            "WHERE m.membershipTier.user.id = :profileId " +
            "AND m.startDate <= :endDate")
    Long countDistinctMembersUpToDate(
            @Param("profileId") String profileId,
            @Param("endDate") LocalDateTime endDate);
}
