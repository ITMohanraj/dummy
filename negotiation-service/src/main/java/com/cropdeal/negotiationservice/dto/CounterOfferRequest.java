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
public class CounterOfferRequest {
    @NotNull
    @Positive
    private BigDecimal counterPricePerKg;
    private String role; // "FARMER" or "DEALER"
}