package com.cropdeal.walletservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long walletId;

    private String referenceId; // orderId or deliveryId
    private String referenceType; // CROP_PAYMENT, DELIVERY_ESCROW, ESCROW_RELEASE, TOPUP

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String type; // CREDIT, DEBIT, HELD, RELEASED

    private String status; // SUCCESS, FAILED
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "SUCCESS";
        }
    }
}
