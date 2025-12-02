package com.hoaithi.profile_service.repository;

import com.hoaithi.profile_service.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by transaction ID
     * @param transactionId PayPal transaction ID
     * @return Optional of Payment
     */
    Optional<Payment> findByTransactionId(String transactionId);
}