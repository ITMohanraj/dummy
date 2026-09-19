package com.cropdeal.walletservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscrowDisputeRequest {
    private Long escrowId;
    private Long orderId;
    private Long deliveryId;
    @NotNull
    private Long disputedBy;
    @NotNull
    private String reason;
}
