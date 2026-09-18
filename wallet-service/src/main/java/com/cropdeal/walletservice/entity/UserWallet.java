package com.cropdeal.walletservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_wallets", uniqueConstraints = {
    @UniqueConstraint(columnNames = "userId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    private String role; // FARMER, DEALER, DELIVERY_PARTNER

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false)
    private String currency; // INR

    private String status; // ACTIVE, FROZEN

    @Version
    private Long version;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }
        if (this.currency == null) {
            this.currency = "INR";
        }
        if (this.status == null) {
            this.status = "ACTIVE";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
