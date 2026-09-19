package com.cropdeal.biddingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketBidMessage {
    private String type; // BID_PLACED, AUCTION_CREATED, AUCTION_ACCEPTED, AUCTION_CLOSED
    private Long auctionId;
    private Long bidId;
    private Long dealerId;
    private BigDecimal bidPricePerKg;
    private BigDecimal totalBidAmount;
    private BigDecimal currentHighestBid;
    private Long winningDealerId;
    private String auctionStatus;
    private LocalDateTime timestamp;
    private String message;
}
