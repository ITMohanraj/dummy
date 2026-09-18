package com.cropdeal.walletservice.controller;

import com.cropdeal.walletservice.dto.EscrowHoldRequest;
import com.cropdeal.walletservice.dto.EscrowReleaseRequest;
import com.cropdeal.walletservice.entity.WalletTransaction;
import com.cropdeal.walletservice.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wallets/transactions")
@RequiredArgsConstructor
@Tag(name = "Wallet Transactions & Escrow", description = "Transaction History and Delivery Escrow APIs")
public class WalletTransactionController {

    private final WalletService walletService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get transaction history for a user")
    public ResponseEntity<List<WalletTransaction>> getTransactions(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(walletService.getTransactions(userId));
    }

    @PostMapping("/escrow/hold")
    @Operation(summary = "Hold delivery fee in escrow from dealer wallet")
    public ResponseEntity<Map<String, Object>> holdEscrow(@Valid @RequestBody EscrowHoldRequest request) {
        walletService.holdDeliveryEscrow(request);
        return ResponseEntity.ok(Map.of("status", "HELD", "deliveryId", request.getDeliveryId()));
    }

    @PostMapping("/escrow/release")
    @Operation(summary = "Release held delivery fee to delivery partner upon successful delivery")
    public ResponseEntity<Map<String, Object>> releaseEscrow(@Valid @RequestBody EscrowReleaseRequest request) {
        walletService.releaseDeliveryEscrow(request);
        return ResponseEntity.ok(Map.of("status", "RELEASED", "deliveryId", request.getDeliveryId()));
    }
}
