package com.cropdeal.reportservice.controller;

import com.cropdeal.reportservice.entity.DealerReportProjection;
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
@RequestMapping("/api/v1/reports/dealer")
@RequiredArgsConstructor
@Tag(name = "Dealer Reports", description = "Dealer Purchases & Logistics Spending CQRS Read Models")
public class DealerReportController {

    private final ReportService reportService;

    @GetMapping("/{dealerId}")
    @Operation(summary = "Get purchase history and delivery expense analytics for a dealer")
    public ResponseEntity<DealerReportProjection> getDealerReport(@PathVariable("dealerId") Long dealerId) {
        return ResponseEntity.ok(reportService.getDealerReport(dealerId));
    }
}
