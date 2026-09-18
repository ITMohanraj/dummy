package com.cropdeal.orderservice.dto;

import com.cropdeal.orderservice.entity.OrderStatus;
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
public class OrderResponse {
    private Long orderId;
    private Long dealerId;
    private Long farmerId;
    private Long cropId;
    private String cropName;
    private Double quantityKg;
    private BigDecimal pricePerKg;
    private BigDecimal totalAmount;
    private String paymentId;
    private String deliveryId;
    private String orderType;
    private OrderStatus status;
    private LocalDateTime createdAt;
}
