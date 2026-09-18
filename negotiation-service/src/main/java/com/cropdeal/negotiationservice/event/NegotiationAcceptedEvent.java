package com.cropdeal.negotiationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationAcceptedEvent implements Serializable {
    private String eventId;
    private String eventType;
    private Long negotiationId;
    private Long cropListingId;
    private Long dealerId;
    private Long farmerId;
    private Double quantityKg;
    private BigDecimal acceptedPricePerKg;
    private BigDecimal totalAmount;
    private LocalDateTime timestamp;
}