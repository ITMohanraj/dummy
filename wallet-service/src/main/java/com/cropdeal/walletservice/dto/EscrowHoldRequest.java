package com.cropdeal.walletservice.dto;

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
public class EscrowHoldRequest {
    private Long deliveryId;
    @NotNull
    private Long orderId;
    @NotNull
    private Long dealerId;
    private Long beneficiaryId; // Farmer ID or Delivery Partner ID
    private String escrowType; // ORDER_PAYMENT or DELIVERY_FEE
    @NotNull
    @Positive
    private BigDecimal amount;
}
