package com.cropdeal.paymentservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions", uniqueConstraints = {
    @UniqueConstraint(columnNames = "idempotencyKey")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false, unique = true)
    private String transactionReference;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long payerUserId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String paymentMethod; // UPI, DEBIT_CARD, CREDIT_CARD, WALLET

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PaymentStatus.INITIATED;
        }
    }
}
