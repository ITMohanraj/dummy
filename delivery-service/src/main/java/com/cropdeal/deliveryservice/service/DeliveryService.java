package com.cropdeal.deliveryservice.service;

import com.cropdeal.deliveryservice.dto.CreateDeliveryRequestDto;
import com.cropdeal.deliveryservice.entity.DeliveryRequest;
import com.cropdeal.deliveryservice.entity.DeliveryStatus;
import com.cropdeal.deliveryservice.repository.DeliveryRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

    private final DeliveryRequestRepository deliveryRepo;
    private final RabbitTemplate rabbitTemplate;
    private final WebClient.Builder webClientBuilder;

    @Value("${cropdeal.wallet-service.url:http://wallet-service:8087}")
    private String walletServiceUrl;

    @Transactional
    public DeliveryRequest createDeliveryRequest(CreateDeliveryRequestDto dto) {
        BigDecimal rate = BigDecimal.valueOf(10.00); // ₹10 per KM
        BigDecimal charge = rate.multiply(BigDecimal.valueOf(dto.getDistanceKm()));

        DeliveryRequest request = DeliveryRequest.builder()
                .orderId(dto.getOrderId())
                .dealerId(dto.getDealerId())
                .farmerId(dto.getFarmerId())
                .pickupLocation(dto.getPickupLocation())
                .dropLocation(dto.getDropLocation())
                .distanceKm(dto.getDistanceKm())
                .ratePerKm(rate)
                .deliveryCharge(charge)
                .status(DeliveryStatus.AVAILABLE)
                .build();

        DeliveryRequest saved = deliveryRepo.save(request);

        // Hold escrow in wallet-service
        try {
            webClientBuilder.build().post()
                    .uri(walletServiceUrl + "/api/v1/wallets/transactions/escrow/hold")
                    .bodyValue(Map.of(
                            "deliveryId", saved.getId(),
                            "orderId", saved.getOrderId(),
                            "dealerId", saved.getDealerId(),
                            "amount", saved.getDeliveryCharge()
                    ))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.warn("Escrow hold call failed: {}", e.getMessage());
        }

        return saved;
    }

    @Transactional
    public DeliveryRequest acceptDelivery(Long deliveryId, Long deliveryPartnerId) {
        DeliveryRequest delivery = deliveryRepo.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery request not found: " + deliveryId));

        if (delivery.getStatus() != DeliveryStatus.AVAILABLE) {
            throw new RuntimeException("DELIVERY_ALREADY_ASSIGNED: Delivery is no longer available.");
        }

        delivery.setDeliveryPartnerId(deliveryPartnerId);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setAssignedAt(LocalDateTime.now());

        DeliveryRequest saved = deliveryRepo.save(delivery);
        log.info("Delivery ID: {} atomically assigned to Delivery Partner ID: {}", deliveryId, deliveryPartnerId);
        return saved;
    }

    @Transactional
    public DeliveryRequest updateStatus(Long deliveryId, DeliveryStatus status) {
        DeliveryRequest delivery = deliveryRepo.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery request not found: " + deliveryId));

        delivery.setStatus(status);

        if (status == DeliveryStatus.DELIVERED) {
            delivery.setDeliveredAt(LocalDateTime.now());

            // Release held delivery charge to delivery partner wallet
            try {
                webClientBuilder.build().post()
                        .uri(walletServiceUrl + "/api/v1/wallets/transactions/escrow/release")
                        .bodyValue(Map.of(
                                "deliveryId", delivery.getId(),
                                "deliveryPartnerId", delivery.getDeliveryPartnerId()
                        ))
                        .retrieve()
                        .toBodilessEntity()
                        .block();
            } catch (Exception e) {
                log.error("Escrow release call failed: {}", e.getMessage());
            }

            // Publish DELIVERY_DELIVERED event
            try {
                rabbitTemplate.convertAndSend("delivery.exchange", "delivery.delivered", Map.of(
                        "deliveryId", delivery.getId(),
                        "orderId", delivery.getOrderId(),
                        "deliveryPartnerId", delivery.getDeliveryPartnerId()
                ));
            } catch (Exception e) {
                log.warn("Failed to publish delivery.delivered event: {}", e.getMessage());
            }
        } else if (status == DeliveryStatus.CANCELLED) {
            // Re-open delivery for other partners without re-charging dealer
            delivery.setDeliveryPartnerId(null);
            delivery.setStatus(DeliveryStatus.AVAILABLE);
        }

        return deliveryRepo.save(delivery);
    }

    public List<DeliveryRequest> getAvailableDeliveries() {
        return deliveryRepo.findByStatus(DeliveryStatus.AVAILABLE);
    }

    public DeliveryRequest getDeliveryById(Long id) {
        return deliveryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + id));
    }
}
