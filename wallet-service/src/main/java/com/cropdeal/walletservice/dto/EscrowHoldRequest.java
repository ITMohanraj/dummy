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
    @NotNull
    private Long deliveryId;
    @NotNull
    private Long orderId;
    @NotNull
    private Long dealerId;
    @NotNull
    @Positive
    private BigDecimal amount;
}
