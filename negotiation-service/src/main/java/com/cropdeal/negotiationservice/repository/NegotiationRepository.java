package com.cropdeal.negotiationservice.repository;

import com.cropdeal.negotiationservice.entity.CropNegotiation;
import com.cropdeal.negotiationservice.entity.NegotiationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NegotiationRepository extends JpaRepository<CropNegotiation, Long> {
    Optional<CropNegotiation> findByCropListingIdAndDealerId(Long cropListingId, Long dealerId);
    List<CropNegotiation> findByCropListingId(Long cropListingId);
    List<CropNegotiation> findByFarmerId(Long farmerId);
    List<CropNegotiation> findByDealerId(Long dealerId);
    List<CropNegotiation> findByStatus(NegotiationStatus status);
}