package com.cropdeal.orderservice.repository;

import com.cropdeal.orderservice.entity.CropOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CropOrderRepository extends JpaRepository<CropOrder, Long> {
    List<CropOrder> findByDealerId(Long dealerId);
    List<CropOrder> findByFarmerId(Long farmerId);
}
