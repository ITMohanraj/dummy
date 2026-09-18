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
public class PriceValidationResponse {
    private boolean valid;
    private String commodity;
    private BigDecimal referencePrice;
    private BigDecimal minAllowedPrice;
    private BigDecimal maxAllowedPrice;
    private String unit;
    private String message;
}
