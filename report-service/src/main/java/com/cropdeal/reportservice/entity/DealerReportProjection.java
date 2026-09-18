package com.cropdeal.reportservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dealer_report_projections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealerReportProjection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long dealerId;

    private BigDecimal totalPurchases;
    private Double totalQuantityBoughtKg;
    private Long completedOrdersCount;
    private BigDecimal deliveryExpenses;

    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = LocalDateTime.now();
        if (this.totalPurchases == null) this.totalPurchases = BigDecimal.ZERO;
        if (this.totalQuantityBoughtKg == null) this.totalQuantityBoughtKg = 0.0;
        if (this.completedOrdersCount == null) this.completedOrdersCount = 0L;
        if (this.deliveryExpenses == null) this.deliveryExpenses = BigDecimal.ZERO;
    }
}
