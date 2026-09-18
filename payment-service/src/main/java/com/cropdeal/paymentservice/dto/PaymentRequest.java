package com.cropdeal.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
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
public class PaymentRequest {
    @NotBlank
    private String idempotencyKey;
    @NotNull
    private Long orderId;
    @NotNull
    private Long payerUserId;
    @NotNull
    @Positive
    private BigDecimal amount;
    @NotBlank
    private String paymentMethod; // UPI, DEBIT_CARD, CREDIT_CARD, WALLET
}
