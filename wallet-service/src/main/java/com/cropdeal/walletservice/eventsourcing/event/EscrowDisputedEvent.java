package com.cropdeal.walletservice.eventsourcing.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscrowDisputedEvent {
    private Long escrowId;
    private Long orderId;
    private Long disputedBy;
    private String reason;
}
