package com.cropdeal.walletservice.eventsourcing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_store", indexes = {
        @Index(name = "idx_aggregate_id", columnList = "aggregateId"),
        @Index(name = "idx_event_type", columnList = "eventType")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long aggregateId;

    @Column(nullable = false)
    private String aggregateType; // e.g. "USER_WALLET"

    @Column(nullable = false)
    private String eventType; // e.g. "WALLET_CREATED", "BALANCE_CREDITED", "BALANCE_DEBITED", "ESCROW_HELD", "ESCROW_RELEASED"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String eventData; // JSON payload

    @Column(nullable = false)
    private Long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime occurredAt;
}
