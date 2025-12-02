package com.hoaithi.profile_service.repository;

import com.hoaithi.profile_service.entity.MembershipTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipTierRepository extends JpaRepository<MembershipTier, Long> {
    /**
     * Find all membership tiers for a channel
     * @param userId The channel's user ID
     * @return List of membership tiers ordered by price
     */
    List<MembershipTier> findByUserIdOrderByPriceAsc(String userId);

    /**
     * Find active membership tiers for a channel
     * @param userId The channel's user ID
     * @param isActive Whether the tier is active
     * @return List of active membership tiers
     */
    List<MembershipTier> findByUserIdAndIsActive(String userId, boolean isActive);

    Optional<MembershipTier> findById(Long membershipTierId);
}
