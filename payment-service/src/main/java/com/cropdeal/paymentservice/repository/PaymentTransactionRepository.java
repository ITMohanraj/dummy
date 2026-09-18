package com.cropdeal.paymentservice.repository;

import com.cropdeal.paymentservice.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByIdempotencyKey(String idempotencyKey);
    Optional<PaymentTransaction> findByOrderId(Long orderId);
}
