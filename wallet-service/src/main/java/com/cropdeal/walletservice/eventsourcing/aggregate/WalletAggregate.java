package com.cropdeal.walletservice.eventsourcing.aggregate;

import com.cropdeal.walletservice.eventsourcing.entity.DomainEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

@Data
@Slf4j
public class WalletAggregate {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Long userId;
    private String role = "USER";
    private BigDecimal balance = BigDecimal.ZERO;
    private String currency = "INR";
    private String status = "ACTIVE";
    private Long version = 0L;
    private int totalEventsReplayed = 0;

    public void apply(DomainEvent event) {
        this.version = event.getVersion();
        this.totalEventsReplayed++;

        try {
            JsonNode data = MAPPER.readTree(event.getEventData());
            switch (event.getEventType()) {
                case "WALLET_CREATED" -> {
                    this.userId = event.getAggregateId();
                    if (data.has("role")) this.role = data.get("role").asText();
                    if (data.has("initialBalance")) this.balance = new BigDecimal(data.get("initialBalance").asText());
                    if (data.has("currency")) this.currency = data.get("currency").asText();
                }
                case "BALANCE_CREDITED" -> {
                    if (data.has("amount")) {
                        BigDecimal credit = new BigDecimal(data.get("amount").asText());
                        this.balance = this.balance.add(credit);
                    }
                }
                case "BALANCE_DEBITED" -> {
                    if (data.has("amount")) {
                        BigDecimal debit = new BigDecimal(data.get("amount").asText());
                        this.balance = this.balance.subtract(debit);
                    }
                }
                case "ESCROW_HELD" -> {
                    if (data.has("amount")) {
                        BigDecimal held = new BigDecimal(data.get("amount").asText());
                        this.balance = this.balance.subtract(held);
                    }
                }
                case "ESCROW_RELEASED" -> {
                    if (data.has("amount")) {
                        BigDecimal released = new BigDecimal(data.get("amount").asText());
                        this.balance = this.balance.add(released);
                    }
                }
                case "ESCROW_REFUNDED" -> {
                    if (data.has("refundedAmount")) {
                        BigDecimal refunded = new BigDecimal(data.get("refundedAmount").asText());
                        this.balance = this.balance.add(refunded);
                    }
                }
                case "ESCROW_DISPUTED" -> log.info("Escrow disputed recorded on Aggregate: {}", this.userId);
                case "ESCROW_RESOLVED" -> {
                    if (data.has("refundAmount") && !data.get("refundAmount").isNull()) {
                        BigDecimal ref = new BigDecimal(data.get("refundAmount").asText());
                        if (ref.compareTo(BigDecimal.ZERO) > 0 && "DEALER".equalsIgnoreCase(this.role)) {
                            this.balance = this.balance.add(ref);
                        }
                    }
                }
                default -> log.debug("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Failed to replay event {} for aggregate {}: {}", event.getId(), event.getAggregateId(), e.getMessage());
        }
    }

    public static WalletAggregate reconstitute(List<DomainEvent> events) {
        WalletAggregate aggregate = new WalletAggregate();
        for (DomainEvent event : events) {
            aggregate.apply(event);
        }
        return aggregate;
    }
}
