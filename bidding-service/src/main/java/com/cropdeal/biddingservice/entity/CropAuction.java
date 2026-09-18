package com.cropdeal.biddingservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "crop_auctions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropAuction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long farmerId;

    @Column(nullable = false)
    private Long cropId;

    private String cropName;

    @Column(nullable = false)
    private Double quantityKg;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal startingPricePerKg;

    @Column(precision = 10, scale = 2)
    private BigDecimal minimumAcceptablePrice;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuctionStatus status;

    private BigDecimal currentHighestBid;
    private Long winningDealerId;

    @Version
    private Long version;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = AuctionStatus.LIVE;
        }
        if (this.currentHighestBid == null) {
            this.currentHighestBid = this.startingPricePerKg;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
