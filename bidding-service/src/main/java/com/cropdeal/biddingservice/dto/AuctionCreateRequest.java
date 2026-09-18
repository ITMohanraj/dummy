package com.cropdeal.biddingservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionCreateRequest {
    @NotNull
    private Long farmerId;
    @NotNull
    private Long cropId;
    private String cropName;
    @NotNull
    @Positive
    private Double quantityKg;
    @NotNull
    @Positive
    private BigDecimal startingPricePerKg;
    private BigDecimal minimumAcceptablePrice;
    @NotNull
    private LocalDateTime startDateTime;
    @NotNull
    private LocalDateTime endDateTime;
}
