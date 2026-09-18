package com.cropdeal.paymentservice.dto;

import com.cropdeal.paymentservice.entity.PaymentStatus;
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
public class PaymentResponse {
    private Long paymentId;
    private String transactionReference;
    private Long orderId;
    private Long payerUserId;
    private BigDecimal amount;
    private String paymentMethod;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private String message;
}
