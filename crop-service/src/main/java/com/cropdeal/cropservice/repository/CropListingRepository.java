package com.cropdeal.cropservice.repository;

import com.cropdeal.cropservice.entity.CropCategory;
import com.cropdeal.cropservice.entity.CropListing;
import com.cropdeal.cropservice.entity.CropStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CropListingRepository extends JpaRepository<CropListing, Long> {

    List<CropListing> findByFarmerId(Long farmerId);

    Page<CropListing> findByStatus(CropStatus status, Pageable pageable);

    @Query("SELECT c FROM CropListing c WHERE c.status = 'ACTIVE' " +
           "AND (:cropName IS NULL OR LOWER(c.cropName) LIKE LOWER(CONCAT('%', :cropName, '%'))) " +
           "AND (:category IS NULL OR c.category = :category) " +
           "AND (:state IS NULL OR LOWER(c.state) = LOWER(:state)) " +
           "AND (:district IS NULL OR LOWER(c.district) = LOWER(:district)) " +
           "AND (:organic IS NULL OR c.organic = :organic)")
    Page<CropListing> searchMarketplace(
            @Param("cropName") String cropName,
            @Param("category") CropCategory category,
            @Param("state") String state,
            @Param("district") String district,
            @Param("organic") Boolean organic,
            Pageable pageable);

    @Query("SELECT c FROM CropListing c WHERE c.status = 'ACTIVE' AND c.latitude IS NOT NULL AND c.longitude IS NOT NULL")
    List<CropListing> findActiveWithCoordinates();
}
