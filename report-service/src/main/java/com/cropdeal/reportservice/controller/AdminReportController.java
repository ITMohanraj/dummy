package com.cropdeal.reportservice.controller;

import com.cropdeal.reportservice.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Reports", description = "Executive Platform Overview & Analytics APIs")
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping("/summary")
    @Operation(summary = "Get high-level platform administrative report summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(reportService.getAdminPlatformSummary());
    }
}
