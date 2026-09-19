package com.cropdeal.walletservice.controller;

import com.cropdeal.walletservice.dto.DebitRequest;
import com.cropdeal.walletservice.dto.TopUpRequest;
import com.cropdeal.walletservice.entity.UserWallet;
import com.cropdeal.walletservice.eventsourcing.aggregate.WalletAggregate;
import com.cropdeal.walletservice.eventsourcing.entity.DomainEvent;
import com.cropdeal.walletservice.eventsourcing.service.EventSourcedWalletService;
import com.cropdeal.walletservice.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallets & Event Sourcing", description = "Farmer, Dealer & Delivery Partner Wallet and Event Store APIs")
public class WalletController {

    private final WalletService walletService;
    private final EventSourcedWalletService eventSourcedService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user wallet balance or auto-create with initial test funds")
    public ResponseEntity<UserWallet> getWallet(
            @PathVariable("userId") Long userId,
            @RequestParam(required = false) String role) {
        return ResponseEntity.ok(walletService.getOrCreateWallet(userId, role));
    }

    @PostMapping("/user/{userId}/debit")
    @Operation(summary = "Debit or withdraw funds from wallet balance (Generates BALANCE_DEBITED domain event)")
    public ResponseEntity<UserWallet> debit(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody DebitRequest request) {
        return ResponseEntity.ok(walletService.debitBalance(userId, request));
    }

    @PostMapping("/user/{userId}/topup")
    @Operation(summary = "Top up wallet balance (Generates BALANCE_CREDITED domain event)")
    public ResponseEntity<UserWallet> topUp(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody TopUpRequest request) {
        return ResponseEntity.ok(walletService.topUpBalance(userId, request));
    }

    // --- Event Sourcing Transparency Endpoints ---

    @GetMapping("/user/{userId}/events")
    @Operation(summary = "Event Sourcing: Retrieve raw immutable Domain Event stream from Event Store")
    public ResponseEntity<List<DomainEvent>> getEventStream(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(eventSourcedService.getEventStream(userId));
    }

    @GetMapping("/user/{userId}/aggregate")
    @Operation(summary = "Event Sourcing: Rehydrate and reconstitute WalletAggregate dynamically from event stream")
    public ResponseEntity<WalletAggregate> rehydrateAggregate(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(eventSourcedService.rehydrateWallet(userId));
    }
}
