package com.cropdeal.negotiationservice.service;

import com.cropdeal.negotiationservice.dto.CounterOfferRequest;
import com.cropdeal.negotiationservice.dto.NegotiationCreateRequest;
import com.cropdeal.negotiationservice.dto.NegotiationResponse;
import com.cropdeal.negotiationservice.entity.CropNegotiation;
import com.cropdeal.negotiationservice.entity.NegotiationStatus;
import com.cropdeal.negotiationservice.event.NegotiationAcceptedEvent;
import com.cropdeal.negotiationservice.repository.NegotiationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NegotiationService {

    private final NegotiationRepository negRepo;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public NegotiationResponse initiateOrUpdateNegotiation(NegotiationCreateRequest req) {
        CropNegotiation neg = negRepo.findByCropListingIdAndDealerId(req.getCropListingId(), req.getDealerId())
                .orElseGet(() -> CropNegotiation.builder()
                        .cropListingId(req.getCropListingId())
                        .dealerId(req.getDealerId())
                        .farmerId(req.getFarmerId())
                        .quantityKg(req.getQuantityKg())
                        .initialPricePerKg(req.getOfferedPricePerKg())
                        .offerHistory("Dealer started offer: ₹" + req.getOfferedPricePerKg() + "/KG")
                        .status(NegotiationStatus.PENDING)
                        .build());

        if (neg.getStatus() == NegotiationStatus.ACCEPTED) {
            throw new IllegalStateException("Negotiation has already been accepted and locked.");
        }
        if (neg.getStatus() == NegotiationStatus.REJECTED) {
            throw new IllegalStateException("Negotiation was rejected. Please initiate a new negotiation.");
        }

        neg.setCurrentOfferPricePerKg(req.getOfferedPricePerKg());
        neg.setQuantityKg(req.getQuantityKg());
        neg.setLastUpdatedByRole("DEALER");
        neg.setStatus(NegotiationStatus.NEGOTIATING);
        neg.setOfferHistory((neg.getOfferHistory() != null ? neg.getOfferHistory() : "") + " -> Dealer offer: ₹" + req.getOfferedPricePerKg());

        CropNegotiation saved = negRepo.save(neg);
        log.info("Negotiation updated: ID {} for Crop {} by Dealer {}", saved.getId(), saved.getCropListingId(), saved.getDealerId());

        publishEvent("negotiation.created", Map.of(
                "negotiationId", saved.getId(),
                "cropId", saved.getCropListingId(),
                "farmerId", saved.getFarmerId(),
                "dealerId", saved.getDealerId(),
                "offeredPricePerKg", saved.getCurrentOfferPricePerKg(),
                "quantityKg", saved.getQuantityKg(),
                "status", saved.getStatus().name()
        ));

        return mapToResponse(saved);
    }

    @Transactional
    public NegotiationResponse counterOffer(Long negotiationId, CounterOfferRequest req) {
        CropNegotiation neg = negRepo.findById(negotiationId)
                .orElseThrow(() -> new IllegalArgumentException("Negotiation not found with ID: " + negotiationId));

        if (neg.getStatus() == NegotiationStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot counter an already ACCEPTED negotiation.");
        }
        if (neg.getStatus() == NegotiationStatus.REJECTED || neg.getStatus() == NegotiationStatus.CANCELLED) {
            throw new IllegalStateException("Cannot counter a closed negotiation (Status: " + neg.getStatus() + ").");
        }

        String role = req.getRole() != null ? req.getRole().toUpperCase() : "COUNTER";
        neg.setCurrentOfferPricePerKg(req.getCounterPricePerKg());
        neg.setLastUpdatedByRole(role);
        neg.setStatus(NegotiationStatus.COUNTERED);
        neg.setOfferHistory(neg.getOfferHistory() + " -> " + role + " counter: ₹" + req.getCounterPricePerKg());

        CropNegotiation saved = negRepo.save(neg);
        log.info("Negotiation counter-offered: ID {} by {}", saved.getId(), role);

        publishEvent("negotiation.countered", Map.of(
                "negotiationId", saved.getId(),
                "cropId", saved.getCropListingId(),
                "farmerId", saved.getFarmerId(),
                "dealerId", saved.getDealerId(),
                "counterPricePerKg", saved.getCurrentOfferPricePerKg(),
                "role", role,
                "status", saved.getStatus().name()
        ));

        return mapToResponse(saved);
    }

    @Transactional
    public NegotiationResponse acceptNegotiation(Long negotiationId) {
        CropNegotiation neg = negRepo.findById(negotiationId)
                .orElseThrow(() -> new IllegalArgumentException("Negotiation not found with ID: " + negotiationId));

        if (neg.getStatus() == NegotiationStatus.ACCEPTED) {
            throw new IllegalStateException("Negotiation is already accepted.");
        }
        if (neg.getStatus() == NegotiationStatus.REJECTED || neg.getStatus() == NegotiationStatus.CANCELLED) {
            throw new IllegalStateException("Cannot accept a closed negotiation.");
        }

        neg.setStatus(NegotiationStatus.ACCEPTED);
        CropNegotiation saved = negRepo.save(neg);

        BigDecimal total = saved.getCurrentOfferPricePerKg().multiply(BigDecimal.valueOf(saved.getQuantityKg()));

        log.info("Negotiation accepted! ID {}, Final Price: ₹{}/KG, Total: ₹{}", saved.getId(), saved.getCurrentOfferPricePerKg(), total);

        NegotiationAcceptedEvent event = NegotiationAcceptedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("NEGOTIATION_ACCEPTED")
                .negotiationId(saved.getId())
                .cropListingId(saved.getCropListingId())
                .dealerId(saved.getDealerId())
                .farmerId(saved.getFarmerId())
                .quantityKg(saved.getQuantityKg())
                .acceptedPricePerKg(saved.getCurrentOfferPricePerKg())
                .totalAmount(total)
                .timestamp(LocalDateTime.now())
                .build();

        try {
            rabbitTemplate.convertAndSend("negotiation.exchange", "negotiation.accepted", event);
            log.info("Published negotiation.accepted event to RabbitMQ for negotiation ID {}", saved.getId());
        } catch (Exception e) {
            log.error("Failed to publish negotiation.accepted event: {}", e.getMessage());
        }

        return mapToResponse(saved);
    }

    @Transactional
    public NegotiationResponse rejectNegotiation(Long negotiationId) {
        CropNegotiation neg = negRepo.findById(negotiationId)
                .orElseThrow(() -> new IllegalArgumentException("Negotiation not found with ID: " + negotiationId));

        if (neg.getStatus() == NegotiationStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot reject an already accepted negotiation.");
        }

        neg.setStatus(NegotiationStatus.REJECTED);
        CropNegotiation saved = negRepo.save(neg);

        publishEvent("negotiation.rejected", Map.of(
                "negotiationId", saved.getId(),
                "cropId", saved.getCropListingId(),
                "farmerId", saved.getFarmerId(),
                "dealerId", saved.getDealerId(),
                "status", saved.getStatus().name()
        ));

        return mapToResponse(saved);
    }

    public NegotiationResponse getById(Long id) {
        return negRepo.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Negotiation not found with ID: " + id));
    }

    public List<NegotiationResponse> getCropNegotiations(Long cropId) {
        return negRepo.findByCropListingId(cropId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NegotiationResponse> getFarmerNegotiations(Long farmerId) {
        return negRepo.findByFarmerId(farmerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NegotiationResponse> getDealerNegotiations(Long dealerId) {
        return negRepo.findByDealerId(dealerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void publishEvent(String routingKey, Map<String, Object> data) {
        try {
            rabbitTemplate.convertAndSend("negotiation.exchange", routingKey, data);
        } catch (Exception e) {
            log.warn("Failed to publish {} event: {}", routingKey, e.getMessage());
        }
    }

    private NegotiationResponse mapToResponse(CropNegotiation n) {
        BigDecimal total = n.getCurrentOfferPricePerKg() != null && n.getQuantityKg() != null ?
                n.getCurrentOfferPricePerKg().multiply(BigDecimal.valueOf(n.getQuantityKg())) : BigDecimal.ZERO;

        return NegotiationResponse.builder()
                .id(n.getId())
                .cropListingId(n.getCropListingId())
                .dealerId(n.getDealerId())
                .farmerId(n.getFarmerId())
                .quantityKg(n.getQuantityKg())
                .currentOfferPricePerKg(n.getCurrentOfferPricePerKg())
                .totalNegotiatedAmount(total)
                .lastUpdatedByRole(n.getLastUpdatedByRole())
                .status(n.getStatus())
                .offerHistory(n.getOfferHistory())
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build();
    }
}