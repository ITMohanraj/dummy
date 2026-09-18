package com.cropdeal.biddingservice.repository;

import com.cropdeal.biddingservice.entity.AuctionStatus;
import com.cropdeal.biddingservice.entity.CropAuction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CropAuctionRepository extends JpaRepository<CropAuction, Long> {
    List<CropAuction> findByStatus(AuctionStatus status);
    List<CropAuction> findByFarmerId(Long farmerId);
    List<CropAuction> findByStatusAndEndDateTimeBefore(AuctionStatus status, LocalDateTime now);
}
