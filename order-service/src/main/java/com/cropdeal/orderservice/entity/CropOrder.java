package com.cropdeal.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "crop_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long dealerId;

    @Column(nullable = false)
    private Long farmerId;

    @Column(nullable = false)
    private Long cropId;

    private String cropName;

    @Column(nullable = false)
    private Double quantityKg;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerKg;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    private String paymentId;
    private String deliveryId;
    private String orderType; // DIRECT, NEGOTIATED, AUCTION

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = OrderStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
