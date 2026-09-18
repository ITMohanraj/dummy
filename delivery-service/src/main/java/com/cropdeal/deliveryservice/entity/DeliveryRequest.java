package com.cropdeal.deliveryservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long dealerId;

    @Column(nullable = false)
    private Long farmerId;

    private Long deliveryPartnerId;

    @Column(nullable = false)
    private String pickupLocation;

    @Column(nullable = false)
    private String dropLocation;

    @Column(nullable = false)
    private Double distanceKm;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal ratePerKm; // Default ₹10/km

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryCharge;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Version
    private Long version;

    private LocalDateTime requestedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime deliveredAt;

    @PrePersist
    public void prePersist() {
        this.requestedAt = LocalDateTime.now();
        if (this.ratePerKm == null) {
            this.ratePerKm = BigDecimal.valueOf(10.00);
        }
        if (this.deliveryCharge == null && this.distanceKm != null) {
            this.deliveryCharge = this.ratePerKm.multiply(BigDecimal.valueOf(this.distanceKm));
        }
        if (this.status == null) {
            this.status = DeliveryStatus.AVAILABLE;
        }
    }
}
