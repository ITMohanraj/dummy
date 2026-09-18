package com.cropdeal.deliveryservice.repository;

import com.cropdeal.deliveryservice.entity.DeliveryRequest;
import com.cropdeal.deliveryservice.entity.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRequestRepository extends JpaRepository<DeliveryRequest, Long> {
    List<DeliveryRequest> findByStatus(DeliveryStatus status);
    Optional<DeliveryRequest> findByOrderId(Long orderId);
    List<DeliveryRequest> findByDeliveryPartnerId(Long deliveryPartnerId);
}
