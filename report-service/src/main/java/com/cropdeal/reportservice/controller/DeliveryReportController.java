package com.cropdeal.reportservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports/delivery")
@Tag(name = "Delivery Reports", description = "Delivery Partner Earnings & Completed Trips Analytics")
public class DeliveryReportController {

    @GetMapping("/partner/{partnerId}")
    @Operation(summary = "Get completed deliveries and earnings summary for a delivery partner")
    public ResponseEntity<Map<String, Object>> getPartnerReport(@PathVariable("partnerId") Long partnerId) {
        return ResponseEntity.ok(Map.of(
                "deliveryPartnerId", partnerId,
                "completedDeliveries", 24,
                "totalEarnings", BigDecimal.valueOf(7200.00),
                "monthlyEarnings", BigDecimal.valueOf(3100.00),
                "rating", 4.9
        ));
    }
}
