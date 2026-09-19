package com.cropdeal.walletservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscrowResolveRequest {
    @NotNull
    private Long escrowId;
    @NotNull
    private String resolution; // RELEASE_TO_BENEFICIARY, REFUND_TO_DEALER, SPLIT
    private BigDecimal beneficiaryAmount;
    private BigDecimal refundAmount;
    private String notes;
}
