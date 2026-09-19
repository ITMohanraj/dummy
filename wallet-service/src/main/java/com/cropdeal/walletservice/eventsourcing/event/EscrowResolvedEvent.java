package com.cropdeal.walletservice.eventsourcing.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscrowResolvedEvent {
    private Long escrowId;
    private String resolution;
    private BigDecimal beneficiaryAmount;
    private BigDecimal refundAmount;
    private String notes;
}
