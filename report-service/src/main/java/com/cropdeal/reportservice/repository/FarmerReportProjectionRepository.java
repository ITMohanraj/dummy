package com.cropdeal.reportservice.repository;

import com.cropdeal.reportservice.entity.FarmerReportProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FarmerReportProjectionRepository extends JpaRepository<FarmerReportProjection, Long> {
    Optional<FarmerReportProjection> findByFarmerId(Long farmerId);
}
