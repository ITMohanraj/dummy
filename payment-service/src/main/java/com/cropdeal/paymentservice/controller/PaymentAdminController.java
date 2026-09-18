package com.cropdeal.paymentservice.controller;

import com.cropdeal.paymentservice.entity.PaymentTransaction;
import com.cropdeal.paymentservice.repository.PaymentTransactionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments/admin")
@RequiredArgsConstructor
@Tag(name = "Payment Admin", description = "Payment Auditing and Reporting APIs")
public class PaymentAdminController {

    private final PaymentTransactionRepository paymentRepo;

    @GetMapping("/all")
    @Operation(summary = "List all payment transactions for audit")
    public ResponseEntity<List<PaymentTransaction>> getAllTransactions() {
        return ResponseEntity.ok(paymentRepo.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment transaction by ID")
    public ResponseEntity<PaymentTransaction> getById(@PathVariable("id") Long id) {
        return paymentRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
