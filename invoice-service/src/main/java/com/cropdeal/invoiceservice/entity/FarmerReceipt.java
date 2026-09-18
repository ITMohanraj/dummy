package com.cropdeal.invoiceservice.entity;

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
public class FarmerReceipt {
    private Long id;
    private String receiptNumber;
    private Long orderId;
    private Long farmerId;
    private Long dealerId;
    private String cropName;
    private Double quantityKg;
    private BigDecimal pricePerKg;
    private BigDecimal totalAmount;
    private String paymentReference;
    private LocalDateTime issuedAt;
}