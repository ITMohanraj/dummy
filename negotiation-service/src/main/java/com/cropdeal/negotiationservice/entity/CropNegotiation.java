package com.cropdeal.negotiationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "crop_negotiations",
       indexes = {
           @Index(name = "idx_crop_dealer", columnList = "cropListingId, dealerId")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropNegotiation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long cropListingId;

    @Column(nullable = false)
    private Long dealerId;

    @Column(nullable = false)
    private Long farmerId;

    @Column(nullable = false)
    private Double quantityKg;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal currentOfferPricePerKg;

    @Column(precision = 10, scale = 2)
    private BigDecimal initialPricePerKg;

    private String lastUpdatedByRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NegotiationStatus status;

    @Column(length = 4000)
    private String offerHistory;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}