package com.cropdeal.negotiationservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationCreateRequest {
    @NotNull
    private Long cropListingId;
    @NotNull
    private Long dealerId;
    @NotNull
    private Long farmerId;
    @NotNull
    @Positive
    private Double quantityKg;
    @NotNull
    @Positive
    private BigDecimal offeredPricePerKg;
}