package com.cropdeal.negotiationservice.dto;

import com.cropdeal.negotiationservice.entity.NegotiationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationResponse {
    private Long id;
    private Long cropListingId;
    private Long dealerId;
    private Long farmerId;
    private Double quantityKg;
    private BigDecimal currentOfferPricePerKg;
    private BigDecimal totalNegotiatedAmount;
    private String lastUpdatedByRole;
    private NegotiationStatus status;
    private String offerHistory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}