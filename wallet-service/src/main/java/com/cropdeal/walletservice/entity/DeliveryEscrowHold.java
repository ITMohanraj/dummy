package com.cropdeal.walletservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_escrow_holds", uniqueConstraints = {
    @UniqueConstraint(columnNames = "deliveryId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryEscrowHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long deliveryId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long dealerId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String status; // HELD, RELEASED, REFUNDED

    private Long deliveryPartnerId;
    private LocalDateTime createdAt;
    private LocalDateTime releasedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "HELD";
        }
    }
}
