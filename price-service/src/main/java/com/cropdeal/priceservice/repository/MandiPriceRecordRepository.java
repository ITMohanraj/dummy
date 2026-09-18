package com.cropdeal.priceservice.repository;

import com.cropdeal.priceservice.entity.MandiPriceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MandiPriceRecordRepository extends JpaRepository<MandiPriceRecord, Long> {

    @Query("SELECT r FROM MandiPriceRecord r WHERE LOWER(r.commodity) = LOWER(:commodity) " +
           "AND (:state IS NULL OR LOWER(r.state) = LOWER(:state)) " +
           "AND (:district IS NULL OR LOWER(r.district) = LOWER(:district)) " +
           "AND (:grade IS NULL OR LOWER(r.grade) = LOWER(:grade)) " +
           "ORDER BY r.recordDate DESC")
    List<MandiPriceRecord> findMatchingPrices(
            @Param("commodity") String commodity,
            @Param("state") String state,
            @Param("district") String district,
            @Param("grade") String grade);

    List<MandiPriceRecord> findByCommodityIgnoreCase(String commodity);

    List<MandiPriceRecord> findByCommodityIgnoreCaseAndRecordDateAfterOrderByRecordDateAsc(String commodity, LocalDate date);
}
