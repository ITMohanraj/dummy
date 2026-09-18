package com.cropdeal.priceservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceAlertRequest {
    @NotNull
    private Long userId;
    @NotBlank
    private String role;
    @NotBlank
    private String cropName;
    private String grade;
    private String state;
    private String district;
    private BigDecimal expectedMinPrice;
    private BigDecimal expectedMaxPrice;
}
