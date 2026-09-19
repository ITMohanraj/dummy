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
public class BalanceCreditedEvent {
    private Long userId;
    private BigDecimal amount;
    private String referenceId;
    private String referenceType;
    private BigDecimal resultingBalance;
}
