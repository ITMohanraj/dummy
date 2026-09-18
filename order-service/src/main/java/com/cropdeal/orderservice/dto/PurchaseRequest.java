package com.cropdeal.orderservice.dto;

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
public class PurchaseRequest {
    @NotNull
    private Long dealerId;
    @NotNull
    private Long farmerId;
    @NotNull
    private Long cropId;
    private String cropName;
    @NotNull
    @Positive
    private Double quantityKg;
    @NotNull
    @Positive
    private BigDecimal pricePerKg;
    private String paymentMethod; // WALLET, UPI, DEBIT_CARD, CREDIT_CARD
}
