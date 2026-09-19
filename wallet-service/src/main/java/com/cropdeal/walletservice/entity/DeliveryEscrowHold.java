package com.cropdeal.walletservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_escrow_holds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryEscrowHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "escrow_type")
    private String escrowType; // ORDER_PAYMENT, DELIVERY_FEE

    private Long deliveryId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long dealerId;

    private Long beneficiaryId; // Farmer ID or Delivery Partner ID

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String status; // HELD, RELEASED, REFUNDED, DISPUTED, RESOLVED

    private Long deliveryPartnerId;
    
    @Column(length = 1000)
    private String disputeReason;
    
    private Long disputedBy;
    private LocalDateTime disputedAt;
    
    @Column(length = 1000)
    private String resolutionNotes;
    
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime releasedAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = "HELD";
        }
        if (this.escrowType == null) {
            this.escrowType = this.deliveryId != null ? "DELIVERY_FEE" : "ORDER_PAYMENT";
        }
    }
}
