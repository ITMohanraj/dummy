package com.cropdeal.biddingservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidPlacementRequest {
    @NotNull
    private Long auctionId;
    @NotNull
    private Long dealerId;
    @NotNull
    @Positive
    private BigDecimal bidPricePerKg;
}
