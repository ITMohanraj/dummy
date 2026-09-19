package com.cropdeal.walletservice.eventsourcing.service;

import com.cropdeal.walletservice.eventsourcing.aggregate.WalletAggregate;
import com.cropdeal.walletservice.eventsourcing.entity.DomainEvent;
import com.cropdeal.walletservice.eventsourcing.repository.DomainEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventSourcedWalletService {

    private final DomainEventRepository eventRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public DomainEvent appendEvent(Long userId, String eventType, Object eventPayload) {
        try {
            Long currentVersion = eventRepo.countByAggregateIdAndAggregateType(userId, "USER_WALLET");
            String jsonPayload = objectMapper.writeValueAsString(eventPayload);

            DomainEvent domainEvent = DomainEvent.builder()
                    .aggregateId(userId)
                    .aggregateType("USER_WALLET")
                    .eventType(eventType)
                    .eventData(jsonPayload)
                    .version(currentVersion + 1)
                    .build();

            DomainEvent saved = eventRepo.save(domainEvent);
            log.info("EventStore: Appended event {} [v{}] for User: {}", eventType, saved.getVersion(), userId);
            return saved;
        } catch (Exception e) {
            log.error("Failed to append event to EventStore: {}", e.getMessage(), e);
            throw new RuntimeException("Event Sourcing persistence error: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public WalletAggregate rehydrateWallet(Long userId) {
        List<DomainEvent> events = eventRepo.findByAggregateIdAndAggregateTypeOrderByVersionAsc(userId, "USER_WALLET");
        log.info("EventStore: Rehydrating WalletAggregate for User: {} by replaying {} events", userId, events.size());
        return WalletAggregate.reconstitute(events);
    }

    @Transactional(readOnly = true)
    public List<DomainEvent> getEventStream(Long userId) {
        return eventRepo.findByAggregateIdAndAggregateTypeOrderByVersionAsc(userId, "USER_WALLET");
    }
}
