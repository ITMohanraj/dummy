package com.cropdeal.walletservice.controller;

import com.cropdeal.walletservice.dto.DebitRequest;
import com.cropdeal.walletservice.dto.TopUpRequest;
import com.cropdeal.walletservice.entity.UserWallet;
import com.cropdeal.walletservice.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallets", description = "Farmer, Dealer & Delivery Partner Wallet Management")
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user wallet balance or auto-create with initial test funds")
    public ResponseEntity<UserWallet> getWallet(
            @PathVariable("userId") Long userId,
            @RequestParam(required = false) String role) {
        return ResponseEntity.ok(walletService.getOrCreateWallet(userId, role));
    }

    @PostMapping("/user/{userId}/debit")
    @Operation(summary = "Debit or withdraw funds from wallet balance")
    public ResponseEntity<UserWallet> debit(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody DebitRequest request) {
        return ResponseEntity.ok(walletService.debitBalance(userId, request));
    }

    @PostMapping("/user/{userId}/topup")
    @Operation(summary = "Top up wallet balance")
    public ResponseEntity<UserWallet> topUp(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody TopUpRequest request) {
        return ResponseEntity.ok(walletService.topUpBalance(userId, request));
    }
}
