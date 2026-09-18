package com.cropdeal.reviewservice.repository;

import com.cropdeal.reviewservice.entity.FarmerReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FarmerReviewRepository extends JpaRepository<FarmerReview, Long> {
    List<FarmerReview> findByFarmerIdOrderByCreatedAtDesc(Long farmerId);
    Optional<FarmerReview> findByOrderIdAndDealerId(Long orderId, Long dealerId);
}
