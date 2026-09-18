package com.cropdeal.paymentservice.service;

import com.cropdeal.paymentservice.dto.PaymentRequest;
import com.cropdeal.paymentservice.dto.PaymentResponse;
import com.cropdeal.paymentservice.entity.PaymentStatus;
import com.cropdeal.paymentservice.entity.PaymentTransaction;
import com.cropdeal.paymentservice.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentTransactionRepository paymentRepo;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public PaymentResponse processSimulatedPayment(PaymentRequest req) {
        // Idempotency check
        Optional<PaymentTransaction> existing = paymentRepo.findByIdempotencyKey(req.getIdempotencyKey());
        if (existing.isPresent()) {
            log.info("Duplicate payment request detected for idempotencyKey: {}. Returning existing record.", req.getIdempotencyKey());
            return mapToResponse(existing.get(), "Idempotent payment returned");
        }

        String ref = "PAY_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        PaymentTransaction txn = PaymentTransaction.builder()
                .idempotencyKey(req.getIdempotencyKey())
                .transactionReference(ref)
                .orderId(req.getOrderId())
                .payerUserId(req.getPayerUserId())
                .amount(req.getAmount())
                .paymentMethod(req.getPaymentMethod())
                .status(PaymentStatus.SUCCESS)
                .completedAt(LocalDateTime.now())
                .build();

        PaymentTransaction saved = paymentRepo.save(txn);

        // Publish PAYMENT_SUCCESS event
        try {
            rabbitTemplate.convertAndSend("payment.exchange", "payment.success", Map.of(
                    "paymentId", saved.getId(),
                    "transactionReference", saved.getTransactionReference(),
                    "orderId", saved.getOrderId(),
                    "amount", saved.getAmount(),
                    "status", saved.getStatus().name()
            ));
        } catch (Exception e) {
            log.warn("Failed to publish payment.success event: {}", e.getMessage());
        }

        return mapToResponse(saved, "Payment simulated successfully");
    }

    public PaymentResponse getPaymentByOrderId(Long orderId) {
        PaymentTransaction txn = paymentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
        return mapToResponse(txn, "Payment found");
    }

    private PaymentResponse mapToResponse(PaymentTransaction t, String msg) {
        return PaymentResponse.builder()
                .paymentId(t.getId())
                .transactionReference(t.getTransactionReference())
                .orderId(t.getOrderId())
                .payerUserId(t.getPayerUserId())
                .amount(t.getAmount())
                .paymentMethod(t.getPaymentMethod())
                .status(t.getStatus())
                .createdAt(t.getCreatedAt())
                .message(msg)
                .build();
    }
}
