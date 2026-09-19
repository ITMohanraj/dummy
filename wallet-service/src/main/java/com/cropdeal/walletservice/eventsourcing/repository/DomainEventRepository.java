package com.cropdeal.walletservice.eventsourcing.repository;

import com.cropdeal.walletservice.eventsourcing.entity.DomainEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DomainEventRepository extends JpaRepository<DomainEvent, Long> {

    List<DomainEvent> findByAggregateIdAndAggregateTypeOrderByVersionAsc(Long aggregateId, String aggregateType);

    List<DomainEvent> findByAggregateIdOrderByVersionAsc(Long aggregateId);

    Long countByAggregateIdAndAggregateType(Long aggregateId, String aggregateType);
}
