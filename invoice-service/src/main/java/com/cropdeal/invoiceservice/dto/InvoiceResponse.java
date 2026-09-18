package com.cropdeal.invoiceservice.dto;

import com.cropdeal.invoiceservice.entity.InvoiceStatus;
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
public class InvoiceResponse {
    private Long id;
    private Long orderId;
    private String invoiceNumber;
    private Long dealerId;
    private String dealerName;
    private Long farmerId;
    private String farmerName;
    private Long cropId;
    private String cropName;
    private Double quantityKg;
    private BigDecimal pricePerKg;
    private BigDecimal cropSubtotal;
    private BigDecimal deliveryCharge;
    private BigDecimal platformFee;
    private BigDecimal totalAmount;
    private String paymentReference;
    private String paymentStatus;
    private String pickupLocation;
    private String deliveryLocation;
    private InvoiceStatus invoiceStatus;
    private LocalDateTime issuedAt;
}