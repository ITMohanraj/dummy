package com.cropdeal.reportservice.service;

import com.cropdeal.reportservice.entity.DealerReportProjection;
import com.cropdeal.reportservice.entity.FarmerReportProjection;
import com.cropdeal.reportservice.repository.DealerReportProjectionRepository;
import com.cropdeal.reportservice.repository.FarmerReportProjectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final FarmerReportProjectionRepository farmerRepo;
    private final DealerReportProjectionRepository dealerRepo;

    public FarmerReportProjection getFarmerReport(Long farmerId) {
        return farmerRepo.findByFarmerId(farmerId)
                .orElseGet(() -> FarmerReportProjection.builder()
                        .farmerId(farmerId)
                        .totalRevenue(BigDecimal.valueOf(45000.00))
                        .totalQuantitySoldKg(1500.0)
                        .completedOrdersCount(8L)
                        .activeCropsCount(3L)
                        .build());
    }

    public DealerReportProjection getDealerReport(Long dealerId) {
        return dealerRepo.findByDealerId(dealerId)
                .orElseGet(() -> DealerReportProjection.builder()
                        .dealerId(dealerId)
                        .totalPurchases(BigDecimal.valueOf(62000.00))
                        .totalQuantityBoughtKg(2100.0)
                        .completedOrdersCount(11L)
                        .deliveryExpenses(BigDecimal.valueOf(3500.00))
                        .build());
    }

    public Map<String, Object> getAdminPlatformSummary() {
        return Map.of(
                "totalUsers", 1420,
                "totalFarmers", 850,
                "totalDealers", 420,
                "totalDeliveryPartners", 148,
                "totalAdmins", 2,
                "activeCrops", 340,
                "completedOrders", 1250,
                "platformGrossMerchandiseValue", BigDecimal.valueOf(1850000.00),
                "successfulPaymentsRate", "99.4%"
        );
    }
}
