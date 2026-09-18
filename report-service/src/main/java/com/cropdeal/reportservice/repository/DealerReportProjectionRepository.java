package com.cropdeal.reportservice.repository;

import com.cropdeal.reportservice.entity.DealerReportProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DealerReportProjectionRepository extends JpaRepository<DealerReportProjection, Long> {
    Optional<DealerReportProjection> findByDealerId(Long dealerId);
}
