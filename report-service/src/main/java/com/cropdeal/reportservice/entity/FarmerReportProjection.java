package com.cropdeal.reportservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "farmer_report_projections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerReportProjection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long farmerId;

    private BigDecimal totalRevenue;
    private Double totalQuantitySoldKg;
    private Long completedOrdersCount;
    private Long activeCropsCount;

    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = LocalDateTime.now();
        if (this.totalRevenue == null) this.totalRevenue = BigDecimal.ZERO;
        if (this.totalQuantitySoldKg == null) this.totalQuantitySoldKg = 0.0;
        if (this.completedOrdersCount == null) this.completedOrdersCount = 0L;
        if (this.activeCropsCount == null) this.activeCropsCount = 0L;
    }
}
