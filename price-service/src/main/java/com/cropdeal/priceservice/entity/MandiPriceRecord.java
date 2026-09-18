package com.cropdeal.priceservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mandi_price_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MandiPriceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String commodity;

    private String normalizedCommodity;
    private String variety;
    private String grade; // Grade A, B, C

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String market;

    @Column(precision = 10, scale = 2)
    private BigDecimal minPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal modalPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxPrice;

    @Column(nullable = false)
    private String sourceUnit; // QUINTAL, TON, KG, UNKNOWN

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal convertedPricePerKg;

    @Column(nullable = false)
    private LocalDate recordDate;

    private LocalDateTime syncedAt;

    @PrePersist
    public void prePersist() {
        this.syncedAt = LocalDateTime.now();
        if (this.recordDate == null) {
            this.recordDate = LocalDate.now();
        }
    }
}
