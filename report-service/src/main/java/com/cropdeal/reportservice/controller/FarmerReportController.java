package com.cropdeal.reportservice.controller;

import com.cropdeal.reportservice.entity.FarmerReportProjection;
import com.cropdeal.reportservice.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports/farmer")
@RequiredArgsConstructor
@Tag(name = "Farmer Reports", description = "Farmer Sales Analytics & Revenue CQRS Read Models")
public class FarmerReportController {

    private final ReportService reportService;

    @GetMapping("/{farmerId}")
    @Operation(summary = "Get sales, revenue and volume analytics for a farmer")
    public ResponseEntity<FarmerReportProjection> getFarmerReport(@PathVariable("farmerId") Long farmerId) {
        return ResponseEntity.ok(reportService.getFarmerReport(farmerId));
    }
}
