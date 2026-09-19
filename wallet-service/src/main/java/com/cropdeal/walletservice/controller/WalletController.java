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

    // --- Production Escrow Endpoints ---

    @PostMapping("/escrow/hold")
    @Operation(summary = "Hold funds in escrow for Order Crop Purchase or Delivery Transport Fee")
    public ResponseEntity<com.cropdeal.walletservice.entity.DeliveryEscrowHold> holdEscrow(
            @Valid @RequestBody com.cropdeal.walletservice.dto.EscrowHoldRequest request) {
        return ResponseEntity.ok(walletService.holdEscrow(request));
    }

    @PostMapping("/escrow/release")
    @Operation(summary = "Release escrowed funds to Beneficiary (Farmer / Delivery Partner)")
    public ResponseEntity<com.cropdeal.walletservice.entity.DeliveryEscrowHold> releaseEscrow(
            @RequestBody com.cropdeal.walletservice.dto.EscrowReleaseRequest request) {
        return ResponseEntity.ok(walletService.releaseEscrow(request));
    }

    @PostMapping("/escrow/refund")
    @Operation(summary = "Refund escrowed funds back to Dealer")
    public ResponseEntity<com.cropdeal.walletservice.entity.DeliveryEscrowHold> refundEscrow(
            @Valid @RequestBody com.cropdeal.walletservice.dto.EscrowRefundRequest request) {
        return ResponseEntity.ok(walletService.refundEscrow(request));
    }

    @PostMapping("/escrow/dispute")
    @Operation(summary = "Raise dispute on held escrow funds")
    public ResponseEntity<com.cropdeal.walletservice.entity.DeliveryEscrowHold> disputeEscrow(
            @Valid @RequestBody com.cropdeal.walletservice.dto.EscrowDisputeRequest request) {
        return ResponseEntity.ok(walletService.disputeEscrow(request));
    }

    @PostMapping("/escrow/resolve")
    @Operation(summary = "Admin resolution of disputed escrow (RELEASE, REFUND, or SPLIT)")
    public ResponseEntity<com.cropdeal.walletservice.entity.DeliveryEscrowHold> resolveEscrow(
            @Valid @RequestBody com.cropdeal.walletservice.dto.EscrowResolveRequest request) {
        return ResponseEntity.ok(walletService.resolveEscrow(request));
    }

    @GetMapping("/escrow/order/{orderId}")
    @Operation(summary = "Get all escrow holds for an Order")
    public ResponseEntity<List<com.cropdeal.walletservice.entity.DeliveryEscrowHold>> getEscrowsByOrder(
            @PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(walletService.getEscrowsByOrderId(orderId));
    }

    @GetMapping("/escrow/user/{userId}")
    @Operation(summary = "Get all escrow holds initiated by User")
    public ResponseEntity<List<com.cropdeal.walletservice.entity.DeliveryEscrowHold>> getUserEscrows(
            @PathVariable("userId") Long userId) {
        return ResponseEntity.ok(walletService.getUserEscrows(userId));
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
