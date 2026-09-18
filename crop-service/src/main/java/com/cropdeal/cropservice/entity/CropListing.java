package com.cropdeal.cropservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "crop_listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long farmerId;

    @Column(nullable = false)
    private String cropName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CropCategory category;

    private String variety;
    private String grade; // A, B, C

    @Enumerated(EnumType.STRING)
    private QualityGrade quality;

    private boolean organic;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Double quantityKg;

    @Column(nullable = false)
    private Double availableQuantityKg;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerKg;

    private BigDecimal referenceGovernmentPrice;
    private BigDecimal minimumAllowedPrice;
    private BigDecimal maximumAllowedPrice;

    private String state;
    private String district;
    private String location;
    private Double latitude;
    private Double longitude;

    private LocalDate harvestDate;
    private LocalDate expectedHarvestDate;
    private LocalDate listingExpiry;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CropStatus status;

    @Version
    private Long version;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.availableQuantityKg == null) {
            this.availableQuantityKg = this.quantityKg;
        }
        if (this.status == null) {
            this.status = CropStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
