package com.cropdeal.priceservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_alert_subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceAlertSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String role; // FARMER, DEALER

    @Column(nullable = false)
    private String cropName;

    private String grade;
    private String state;
    private String district;

    @Column(precision = 10, scale = 2)
    private BigDecimal expectedMinPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal expectedMaxPrice;

    private boolean active;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }
}
