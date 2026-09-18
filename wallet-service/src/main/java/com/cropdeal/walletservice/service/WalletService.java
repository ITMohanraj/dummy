package com.cropdeal.walletservice.service;

import com.cropdeal.walletservice.dto.EscrowHoldRequest;
import com.cropdeal.walletservice.dto.EscrowReleaseRequest;
import com.cropdeal.walletservice.dto.DebitRequest;
import com.cropdeal.walletservice.dto.TopUpRequest;
import com.cropdeal.walletservice.entity.DeliveryEscrowHold;
import com.cropdeal.walletservice.entity.UserWallet;
import com.cropdeal.walletservice.entity.WalletTransaction;
import com.cropdeal.walletservice.repository.DeliveryEscrowHoldRepository;
import com.cropdeal.walletservice.repository.UserWalletRepository;
import com.cropdeal.walletservice.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final UserWalletRepository walletRepo;
    private final WalletTransactionRepository txnRepo;
    private final DeliveryEscrowHoldRepository escrowRepo;
    private final RabbitTemplate rabbitTemplate;

    public UserWallet getOrCreateWallet(Long userId, String role) {
        return walletRepo.findByUserId(userId)
                .orElseGet(() -> walletRepo.save(UserWallet.builder()
                        .userId(userId)
                        .role(role != null ? role : "USER")
                        .balance(BigDecimal.ZERO) // Initial seed test funds
                        .currency("INR")
                        .status("ACTIVE")
                        .build()));
    }

    @Transactional
    public UserWallet debitBalance(Long userId, DebitRequest req) {
        UserWallet wallet = getOrCreateWallet(userId, "DEALER");
        if (wallet.getBalance().compareTo(req.getAmount()) < 0) {
            throw new RuntimeException("Insufficient wallet balance. Available: ₹" + wallet.getBalance() + ", Requested: ₹" + req.getAmount());
        }

        wallet.setBalance(wallet.getBalance().subtract(req.getAmount()));
        UserWallet saved = walletRepo.save(wallet);

        txnRepo.save(WalletTransaction.builder()
                .walletId(saved.getId())
                .referenceId("WITHDRAW_" + System.currentTimeMillis())
                .referenceType(req.getReason() != null ? req.getReason() : "WITHDRAW")
                .amount(req.getAmount())
                .type("DEBIT")
                .status("SUCCESS")
                .build());

        return saved;
    }

    public UserWallet topUpBalance(Long userId, TopUpRequest req) {
        UserWallet wallet = getOrCreateWallet(userId, "DEALER");
        wallet.setBalance(wallet.getBalance().add(req.getAmount()));
        UserWallet saved = walletRepo.save(wallet);

        txnRepo.save(WalletTransaction.builder()
                .walletId(saved.getId())
                .referenceId("TOPUP_" + System.currentTimeMillis())
                .referenceType("TOPUP")
                .amount(req.getAmount())
                .type("CREDIT")
                .status("SUCCESS")
                .build());

        return saved;
    }

    @Transactional
    public void holdDeliveryEscrow(EscrowHoldRequest req) {
        UserWallet dealerWallet = getOrCreateWallet(req.getDealerId(), "DEALER");
        if (dealerWallet.getBalance().compareTo(req.getAmount()) < 0) {
            throw new RuntimeException("Insufficient wallet balance for delivery fee: ₹" + req.getAmount());
        }

        // Debit dealer wallet
        dealerWallet.setBalance(dealerWallet.getBalance().subtract(req.getAmount()));
        walletRepo.save(dealerWallet);

        txnRepo.save(WalletTransaction.builder()
                .walletId(dealerWallet.getId())
                .referenceId("DELIVERY_" + req.getDeliveryId())
                .referenceType("DELIVERY_ESCROW")
                .amount(req.getAmount())
                .type("HELD")
                .status("SUCCESS")
                .build());

        // Record Escrow
        DeliveryEscrowHold hold = DeliveryEscrowHold.builder()
                .deliveryId(req.getDeliveryId())
                .orderId(req.getOrderId())
                .dealerId(req.getDealerId())
                .amount(req.getAmount())
                .status("HELD")
                .build();
        escrowRepo.save(hold);
        log.info("Delivery escrow of ₹{} held for Delivery ID: {}", req.getAmount(), req.getDeliveryId());
    }

    @Transactional
    public void releaseDeliveryEscrow(EscrowReleaseRequest req) {
        DeliveryEscrowHold hold = escrowRepo.findByDeliveryId(req.getDeliveryId())
                .orElseThrow(() -> new RuntimeException("No escrow hold found for delivery: " + req.getDeliveryId()));

        if ("RELEASED".equals(hold.getStatus())) {
            log.info("Escrow for delivery {} already released. Idempotent skip.", req.getDeliveryId());
            return;
        }

        UserWallet partnerWallet = getOrCreateWallet(req.getDeliveryPartnerId(), "DELIVERY_PARTNER");
        partnerWallet.setBalance(partnerWallet.getBalance().add(hold.getAmount()));
        walletRepo.save(partnerWallet);

        txnRepo.save(WalletTransaction.builder()
                .walletId(partnerWallet.getId())
                .referenceId("DELIVERY_" + req.getDeliveryId())
                .referenceType("ESCROW_RELEASE")
                .amount(hold.getAmount())
                .type("CREDIT")
                .status("SUCCESS")
                .build());

        hold.setStatus("RELEASED");
        hold.setDeliveryPartnerId(req.getDeliveryPartnerId());
        hold.setReleasedAt(LocalDateTime.now());
        escrowRepo.save(hold);

        log.info("Delivery escrow released! ₹{} credited to Delivery Partner: {}", hold.getAmount(), req.getDeliveryPartnerId());
    }

    public List<WalletTransaction> getTransactions(Long userId) {
        UserWallet wallet = getOrCreateWallet(userId, "USER");
        return txnRepo.findByWalletIdOrderByCreatedAtDesc(wallet.getId());
    }
}
