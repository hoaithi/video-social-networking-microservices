package com.hoaithi.profile_service.repository;

import com.hoaithi.profile_service.entity.Payment;
import com.hoaithi.profile_service.enums.PaymentStatus;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by transaction ID
     * @param transactionId PayPal transaction ID
     * @return Optional of Payment
     */
    Optional<Payment> findByTransactionId(String transactionId);

    // New count methods - Fixed to use correct field path
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.membership.user.id = :profileId")
    Long countByProfileId(@Param("profileId") String profileId);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.membership.user.id = :profileId AND p.paymentStatus = :status")
    Long countByProfileIdAndStatus(@Param("profileId") String profileId, @Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.membership.user.id = :profileId AND p.paymentStatus = 'COMPLETED'")
    BigDecimal sumAmountByProfileId(@Param("profileId") String profileId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.membership.membershipTier.user.id = :profileId AND p.paymentStatus = 'COMPLETED'")
    BigDecimal sumRevenueByChannelId(@Param("profileId") String profileId);


    // NEW: Monthly statistics queries for channel revenue
    @Query("SELECT YEAR(p.paymentDate) as year, MONTH(p.paymentDate) as month, " +
            "COUNT(p) as paymentCount, SUM(p.amount) as totalRevenue " +
            "FROM Payment p " +
            "WHERE p.membership.membershipTier.user.id = :profileId " +
            "AND p.paymentStatus = 'COMPLETED' " +
            "GROUP BY YEAR(p.paymentDate), MONTH(p.paymentDate) " +
            "ORDER BY YEAR(p.paymentDate), MONTH(p.paymentDate)")
    List<Object[]> getMonthlyRevenueByChannelId(@Param("profileId") String profileId);

    // Get payments within date range for detailed analysis
    @Query("SELECT p FROM Payment p " +
            "WHERE p.membership.membershipTier.user.id = :profileId " +
            "AND p.paymentStatus = 'COMPLETED' " +
            "AND p.paymentDate BETWEEN :startDate AND :endDate " +
            "ORDER BY p.paymentDate ASC")
    List<Payment> findCompletedPaymentsByChannelIdAndDateRange(
            @Param("profileId") String profileId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Get first and last payment dates
    @Query("SELECT MIN(p.paymentDate), MAX(p.paymentDate) FROM Payment p " +
            "WHERE p.membership.membershipTier.user.id = :profileId " +
            "AND p.paymentStatus = 'COMPLETED'")
    Object[] getPaymentDateRange(@Param("profileId") String profileId);
}