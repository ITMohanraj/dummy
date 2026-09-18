package com.cropdeal.paymentservice.controller;

import com.cropdeal.paymentservice.dto.PaymentRequest;
import com.cropdeal.paymentservice.dto.PaymentResponse;
import com.cropdeal.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Simulated Payment Processing APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/simulate")
    @Operation(summary = "Process simulated payment with idempotency key")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processSimulatedPayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment transaction details by order ID")
    public ResponseEntity<PaymentResponse> getByOrderId(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }
}
