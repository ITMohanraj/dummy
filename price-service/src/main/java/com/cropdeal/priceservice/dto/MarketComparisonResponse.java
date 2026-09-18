package com.cropdeal.priceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketComparisonResponse {
    private String commodity;
    private BigDecimal overallMinPricePerKg;
    private BigDecimal overallMaxPricePerKg;
    private BigDecimal overallAvgPricePerKg;
    private List<MarketPriceItem> markets;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketPriceItem {
        private String market;
        private String state;
        private String district;
        private BigDecimal minPrice;
        private BigDecimal modalPrice;
        private BigDecimal maxPrice;
        private BigDecimal pricePerKg;
    }
}
