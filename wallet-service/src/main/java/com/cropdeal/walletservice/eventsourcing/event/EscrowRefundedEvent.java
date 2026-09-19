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
public class EscrowRefundedEvent {
    private Long escrowId;
    private Long orderId;
    private Long dealerId;
    private BigDecimal refundedAmount;
    private String reason;
}
