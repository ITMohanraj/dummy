package com.cropdeal.walletservice.repository;

import com.cropdeal.walletservice.entity.DeliveryEscrowHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryEscrowHoldRepository extends JpaRepository<DeliveryEscrowHold, Long> {
    Optional<DeliveryEscrowHold> findByDeliveryId(Long deliveryId);
}
