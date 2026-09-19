package com.cropdeal.walletservice.service;

import com.cropdeal.walletservice.dto.DebitRequest;
import com.cropdeal.walletservice.dto.EscrowHoldRequest;
import com.cropdeal.walletservice.dto.EscrowReleaseRequest;
import com.cropdeal.walletservice.dto.TopUpRequest;
import com.cropdeal.walletservice.entity.DeliveryEscrowHold;
import com.cropdeal.walletservice.entity.UserWallet;
import com.cropdeal.walletservice.entity.WalletTransaction;
import com.cropdeal.walletservice.eventsourcing.event.*;
import com.cropdeal.walletservice.eventsourcing.service.EventSourcedWalletService;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final UserWalletRepository walletRepo;
    private final WalletTransactionRepository txnRepo;
    private final DeliveryEscrowHoldRepository escrowRepo;
    private final RabbitTemplate rabbitTemplate;
    private final EventSourcedWalletService eventSourcedService;

    @Transactional
    public UserWallet getOrCreateWallet(Long userId, String role) {
        return walletRepo.findByUserId(userId)
                .orElseGet(() -> {
                    UserWallet newWallet = walletRepo.save(UserWallet.builder()
                            .userId(userId)
                            .role(role != null ? role : "USER")
                            .balance(BigDecimal.ZERO)
                            .currency("INR")
                            .status("ACTIVE")
                            .build());

                    // Append WALLET_CREATED to Event Store
                    eventSourcedService.appendEvent(userId, "WALLET_CREATED", WalletCreatedEvent.builder()
                            .userId(userId)
                            .role(newWallet.getRole())
                            .initialBalance(newWallet.getBalance())
                            .currency(newWallet.getCurrency())
                            .build());

                    return newWallet;
                });
    }

    @Transactional
    public UserWallet debitBalance(Long userId, DebitRequest req) {
        UserWallet wallet = getOrCreateWallet(userId, "DEALER");
        if (wallet.getBalance().compareTo(req.getAmount()) < 0) {
            throw new RuntimeException("Insufficient wallet balance. Available: ?" + wallet.getBalance() + ", Requested: ?" + req.getAmount());
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

        // Append BALANCE_DEBITED to Event Store
        eventSourcedService.appendEvent(userId, "BALANCE_DEBITED", BalanceDebitedEvent.builder()
                .userId(userId)
                .amount(req.getAmount())
                .referenceId("WITHDRAW_" + System.currentTimeMillis())
                .referenceType(req.getReason() != null ? req.getReason() : "WITHDRAW")
                .resultingBalance(saved.getBalance())
                .build());

        return saved;
    }

    @Transactional
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

        // Append BALANCE_CREDITED to Event Store
        eventSourcedService.appendEvent(userId, "BALANCE_CREDITED", BalanceCreditedEvent.builder()
                .userId(userId)
                .amount(req.getAmount())
                .referenceId("TOPUP_" + System.currentTimeMillis())
                .referenceType("TOPUP")
                .resultingBalance(saved.getBalance())
                .build());

        return saved;
    }

    @Transactional
    public void holdDeliveryEscrow(EscrowHoldRequest req) {
        holdEscrow(req);
    }

    @Transactional
    public DeliveryEscrowHold holdEscrow(EscrowHoldRequest req) {
        UserWallet dealerWallet = getOrCreateWallet(req.getDealerId(), "DEALER");
        if (dealerWallet.getBalance().compareTo(req.getAmount()) < 0) {
            throw new RuntimeException("Insufficient wallet balance for escrow hold: ₹" + req.getAmount() + " (Available: ₹" + dealerWallet.getBalance() + ")");
        }

        // Debit dealer wallet
        dealerWallet.setBalance(dealerWallet.getBalance().subtract(req.getAmount()));
        walletRepo.save(dealerWallet);

        String refType = req.getEscrowType() != null ? req.getEscrowType() : (req.getDeliveryId() != null ? "DELIVERY_ESCROW" : "ORDER_PAYMENT_ESCROW");
        String refId = req.getDeliveryId() != null ? "DELIVERY_" + req.getDeliveryId() : "ORDER_" + req.getOrderId();

        txnRepo.save(WalletTransaction.builder()
                .walletId(dealerWallet.getId())
                .referenceId(refId)
                .referenceType(refType)
                .amount(req.getAmount())
                .type("HELD")
                .status("SUCCESS")
                .build());

        // Record Escrow Hold
        DeliveryEscrowHold hold = DeliveryEscrowHold.builder()
                .deliveryId(req.getDeliveryId())
                .orderId(req.getOrderId())
                .dealerId(req.getDealerId())
                .beneficiaryId(req.getBeneficiaryId())
                .deliveryPartnerId(req.getDeliveryId() != null ? req.getBeneficiaryId() : null)
                .escrowType(refType)
                .amount(req.getAmount())
                .status("HELD")
                .createdAt(LocalDateTime.now())
                .build();
        DeliveryEscrowHold saved = escrowRepo.save(hold);

        // Append ESCROW_HELD to Event Store
        eventSourcedService.appendEvent(req.getDealerId(), "ESCROW_HELD", EscrowHeldEvent.builder()
                .deliveryId(req.getDeliveryId())
                .orderId(req.getOrderId())
                .dealerId(req.getDealerId())
                .amount(req.getAmount())
                .build());

        log.info("Production escrow of ₹{} held (Type: {}) for Order: {}, Delivery: {}",
                req.getAmount(), refType, req.getOrderId(), req.getDeliveryId());
        return saved;
    }

    @Transactional
    public void releaseDeliveryEscrow(EscrowReleaseRequest req) {
        releaseEscrow(req);
    }

    @Transactional
    public DeliveryEscrowHold releaseEscrow(EscrowReleaseRequest req) {
        DeliveryEscrowHold hold;
        if (req.getEscrowId() != null) {
            hold = escrowRepo.findById(req.getEscrowId())
                    .orElseThrow(() -> new RuntimeException("Escrow hold not found: " + req.getEscrowId()));
        } else if (req.getDeliveryId() != null) {
            hold = escrowRepo.findByDeliveryId(req.getDeliveryId())
                    .orElseThrow(() -> new RuntimeException("No escrow hold found for delivery: " + req.getDeliveryId()));
        } else if (req.getOrderId() != null) {
            List<DeliveryEscrowHold> holds = escrowRepo.findByOrderId(req.getOrderId());
            hold = holds.stream().filter(h -> "HELD".equals(h.getStatus()) || "DISPUTED".equals(h.getStatus()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No active escrow hold found for order: " + req.getOrderId()));
        } else {
            throw new RuntimeException("Must specify escrowId, deliveryId, or orderId for escrow release");
        }

        if ("RELEASED".equals(hold.getStatus())) {
            log.info("Escrow #{} already released. Idempotent return.", hold.getId());
            return hold;
        }

        Long beneficiaryId = req.getBeneficiaryId() != null ? req.getBeneficiaryId() :
                (req.getDeliveryPartnerId() != null ? req.getDeliveryPartnerId() : hold.getBeneficiaryId());

        if (beneficiaryId == null) {
            beneficiaryId = hold.getDeliveryPartnerId();
        }

        if (beneficiaryId == null) {
            throw new RuntimeException("Beneficiary user ID must be provided to disburse released escrow funds");
        }

        String beneficiaryRole = "DELIVERY_FEE".equals(hold.getEscrowType()) ? "DELIVERY_PARTNER" : "FARMER";
        UserWallet beneficiaryWallet = getOrCreateWallet(beneficiaryId, beneficiaryRole);
        beneficiaryWallet.setBalance(beneficiaryWallet.getBalance().add(hold.getAmount()));
        walletRepo.save(beneficiaryWallet);

        txnRepo.save(WalletTransaction.builder()
                .walletId(beneficiaryWallet.getId())
                .referenceId("ESCROW_" + hold.getId())
                .referenceType("ESCROW_RELEASE")
                .amount(hold.getAmount())
                .type("CREDIT")
                .status("SUCCESS")
                .build());

        hold.setStatus("RELEASED");
        hold.setBeneficiaryId(beneficiaryId);
        if ("DELIVERY_FEE".equals(hold.getEscrowType())) {
            hold.setDeliveryPartnerId(beneficiaryId);
        }
        hold.setReleasedAt(LocalDateTime.now());
        DeliveryEscrowHold saved = escrowRepo.save(hold);

        // Append ESCROW_RELEASED to Event Store
        eventSourcedService.appendEvent(beneficiaryId, "ESCROW_RELEASED", EscrowReleasedEvent.builder()
                .deliveryId(hold.getDeliveryId())
                .deliveryPartnerId(beneficiaryId)
                .amount(hold.getAmount())
                .build());

        log.info("Escrow #{} released! ₹{} credited to Beneficiary {}: {}",
                hold.getId(), hold.getAmount(), beneficiaryRole, beneficiaryId);
        return saved;
    }

    @Transactional
    public DeliveryEscrowHold refundEscrow(com.cropdeal.walletservice.dto.EscrowRefundRequest req) {
        DeliveryEscrowHold hold;
        if (req.getEscrowId() != null) {
            hold = escrowRepo.findById(req.getEscrowId())
                    .orElseThrow(() -> new RuntimeException("Escrow hold not found: " + req.getEscrowId()));
        } else if (req.getOrderId() != null) {
            List<DeliveryEscrowHold> holds = escrowRepo.findByOrderId(req.getOrderId());
            hold = holds.stream().filter(h -> "HELD".equals(h.getStatus()) || "DISPUTED".equals(h.getStatus()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No refundable escrow found for order: " + req.getOrderId()));
        } else {
            throw new RuntimeException("Must specify escrowId or orderId to refund");
        }

        if ("REFUNDED".equals(hold.getStatus())) {
            log.info("Escrow #{} already refunded. Idempotent return.", hold.getId());
            return hold;
        }

        // Credit dealer wallet
        UserWallet dealerWallet = getOrCreateWallet(req.getDealerId(), "DEALER");
        dealerWallet.setBalance(dealerWallet.getBalance().add(hold.getAmount()));
        walletRepo.save(dealerWallet);

        txnRepo.save(WalletTransaction.builder()
                .walletId(dealerWallet.getId())
                .referenceId("ESCROW_REFUND_" + hold.getId())
                .referenceType("ESCROW_REFUND")
                .amount(hold.getAmount())
                .type("CREDIT")
                .status("SUCCESS")
                .build());

        hold.setStatus("REFUNDED");
        hold.setResolutionNotes("Refunded to dealer: " + (req.getReason() != null ? req.getReason() : "Order cancelled / Delivery aborted"));
        hold.setResolvedAt(LocalDateTime.now());
        DeliveryEscrowHold saved = escrowRepo.save(hold);

        // Append ESCROW_REFUNDED to Event Store
        eventSourcedService.appendEvent(req.getDealerId(), "ESCROW_REFUNDED", EscrowRefundedEvent.builder()
                .escrowId(hold.getId())
                .orderId(hold.getOrderId())
                .dealerId(req.getDealerId())
                .refundedAmount(hold.getAmount())
                .reason(req.getReason())
                .build());

        log.info("Escrow #{} refunded! ₹{} credited back to Dealer {}", hold.getId(), hold.getAmount(), req.getDealerId());
        return saved;
    }

    @Transactional
    public DeliveryEscrowHold disputeEscrow(com.cropdeal.walletservice.dto.EscrowDisputeRequest req) {
        DeliveryEscrowHold hold;
        if (req.getEscrowId() != null) {
            hold = escrowRepo.findById(req.getEscrowId())
                    .orElseThrow(() -> new RuntimeException("Escrow hold not found: " + req.getEscrowId()));
        } else if (req.getOrderId() != null) {
            List<DeliveryEscrowHold> holds = escrowRepo.findByOrderId(req.getOrderId());
            hold = holds.stream().filter(h -> "HELD".equals(h.getStatus()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No active escrow found to dispute for order: " + req.getOrderId()));
        } else {
            throw new RuntimeException("Must provide escrowId or orderId to dispute");
        }

        hold.setStatus("DISPUTED");
        hold.setDisputeReason(req.getReason());
        hold.setDisputedBy(req.getDisputedBy());
        hold.setDisputedAt(LocalDateTime.now());
        DeliveryEscrowHold saved = escrowRepo.save(hold);

        // Append ESCROW_DISPUTED to Event Store
        eventSourcedService.appendEvent(req.getDisputedBy(), "ESCROW_DISPUTED", EscrowDisputedEvent.builder()
                .escrowId(hold.getId())
                .orderId(hold.getOrderId())
                .disputedBy(req.getDisputedBy())
                .reason(req.getReason())
                .build());

        log.warn("Escrow #{} is now DISPUTED by User {}. Reason: {}", hold.getId(), req.getDisputedBy(), req.getReason());
        return saved;
    }

    @Transactional
    public DeliveryEscrowHold resolveEscrow(com.cropdeal.walletservice.dto.EscrowResolveRequest req) {
        DeliveryEscrowHold hold = escrowRepo.findById(req.getEscrowId())
                .orElseThrow(() -> new RuntimeException("Escrow hold not found: " + req.getEscrowId()));

        if (!"DISPUTED".equalsIgnoreCase(hold.getStatus()) && !"HELD".equalsIgnoreCase(hold.getStatus())) {
            throw new RuntimeException("Cannot resolve escrow in status: " + hold.getStatus());
        }

        String resolution = req.getResolution();
        if ("REFUND_TO_DEALER".equalsIgnoreCase(resolution)) {
            refundEscrow(com.cropdeal.walletservice.dto.EscrowRefundRequest.builder()
                    .escrowId(hold.getId())
                    .dealerId(hold.getDealerId())
                    .reason("Dispute resolution: Full refund - " + req.getNotes())
                    .build());
        } else if ("RELEASE_TO_BENEFICIARY".equalsIgnoreCase(resolution)) {
            releaseEscrow(com.cropdeal.walletservice.dto.EscrowReleaseRequest.builder()
                    .escrowId(hold.getId())
                    .beneficiaryId(hold.getBeneficiaryId() != null ? hold.getBeneficiaryId() : hold.getDeliveryPartnerId())
                    .build());
        } else if ("SPLIT".equalsIgnoreCase(resolution)) {
            BigDecimal benAmt = req.getBeneficiaryAmount() != null ? req.getBeneficiaryAmount() : hold.getAmount().divide(BigDecimal.valueOf(2));
            BigDecimal refAmt = req.getRefundAmount() != null ? req.getRefundAmount() : hold.getAmount().subtract(benAmt);

            // Credit beneficiary
            Long benId = hold.getBeneficiaryId() != null ? hold.getBeneficiaryId() : hold.getDeliveryPartnerId();
            if (benId != null && benAmt.compareTo(BigDecimal.ZERO) > 0) {
                UserWallet benWallet = getOrCreateWallet(benId, "BENEFICIARY");
                benWallet.setBalance(benWallet.getBalance().add(benAmt));
                walletRepo.save(benWallet);
            }

            // Credit dealer refund
            if (refAmt.compareTo(BigDecimal.ZERO) > 0) {
                UserWallet dealerWallet = getOrCreateWallet(hold.getDealerId(), "DEALER");
                dealerWallet.setBalance(dealerWallet.getBalance().add(refAmt));
                walletRepo.save(dealerWallet);
            }

            hold.setStatus("RESOLVED");
            hold.setResolutionNotes("Split resolution: Beneficiary ₹" + benAmt + ", Dealer ₹" + refAmt + ". " + req.getNotes());
            hold.setResolvedAt(LocalDateTime.now());
            escrowRepo.save(hold);
        }

        // Append ESCROW_RESOLVED to Event Store
        eventSourcedService.appendEvent(hold.getDealerId(), "ESCROW_RESOLVED", EscrowResolvedEvent.builder()
                .escrowId(hold.getId())
                .resolution(resolution)
                .beneficiaryAmount(req.getBeneficiaryAmount())
                .refundAmount(req.getRefundAmount())
                .notes(req.getNotes())
                .build());

        return hold;
    }

    public List<DeliveryEscrowHold> getEscrowsByOrderId(Long orderId) {
        return escrowRepo.findByOrderId(orderId);
    }

    public List<DeliveryEscrowHold> getUserEscrows(Long userId) {
        return escrowRepo.findByDealerId(userId);
    }

    public List<WalletTransaction> getTransactions(Long userId) {
        UserWallet wallet = getOrCreateWallet(userId, "USER");
        return txnRepo.findByWalletIdOrderByCreatedAtDesc(wallet.getId());
    }
}
