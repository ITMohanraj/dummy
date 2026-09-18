package com.cropdeal.priceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceTrendResponse {
    private String commodity;
    private String state;
    private String district;
    private String trend; // UP, DOWN, STABLE
    private BigDecimal currentPrice;
    private BigDecimal previousPrice;
    private BigDecimal percentageChange;
}
