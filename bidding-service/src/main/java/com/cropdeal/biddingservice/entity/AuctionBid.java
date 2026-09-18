package com.cropdeal.biddingservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "auction_bids")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionBid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long auctionId;

    @Column(nullable = false)
    private Long dealerId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal bidPricePerKg;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalBidAmount;

    @Column(nullable = false)
    private String status; // VALID, OUTBID, ACCEPTED

    private LocalDateTime bidTime;

    @PrePersist
    public void prePersist() {
        this.bidTime = LocalDateTime.now();
        if (this.status == null) {
            this.status = "VALID";
        }
    }
}
