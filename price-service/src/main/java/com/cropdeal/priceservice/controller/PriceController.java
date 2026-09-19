package com.cropdeal.priceservice.controller;

import com.cropdeal.priceservice.dto.PriceValidationResponse;
import com.cropdeal.priceservice.service.MandiSyncScheduler;
import com.cropdeal.priceservice.service.PriceEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/prices")
@RequiredArgsConstructor
@Tag(name = "Government Prices", description = "Mandi Price Validation and Official data.gov.in Sync APIs")
public class PriceController {

    private final PriceEngineService priceService;
    private final MandiSyncScheduler mandiSyncScheduler;
    private final com.cropdeal.priceservice.repository.MandiPriceRecordRepository mandiRepo;

    @GetMapping("/validate")
    @Operation(summary = "Validate farmer crop price per KG against Government Mandi ranges")
    public ResponseEntity<PriceValidationResponse> validatePrice(
            @RequestParam("cropName") String cropName,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String district,
            @RequestParam(defaultValue = "A") String grade,
            @RequestParam("pricePerKg") BigDecimal pricePerKg) {
        return ResponseEntity.ok(priceService.validateFarmerPrice(cropName, state, district, grade, pricePerKg));
    }

    @GetMapping("/mandi-rates")
    @Operation(summary = "Get all latest Government Mandi market price records")
    public ResponseEntity<java.util.List<com.cropdeal.priceservice.entity.MandiPriceRecord>> getMandiRates(
            @RequestParam(required = false) String commodity) {
        if (commodity != null && !commodity.isBlank()) {
            return ResponseEntity.ok(mandiRepo.findByCommodityIgnoreCase(commodity));
        }
        return ResponseEntity.ok(mandiRepo.findAll());
    }

    @PostMapping("/sync-government")
    @Operation(summary = "Trigger on-demand live sync with official Government Agmarknet (data.gov.in) Portal")
    public ResponseEntity<Map<String, Object>> syncGovernmentPrices() {
        int count = mandiSyncScheduler.syncWithOfficialGovernmentPortal();
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Government Mandi market prices synchronized successfully",
                "recordsSynced", count,
                "source", "data.gov.in / Agmarknet Mandi Daily Rates API"
        ));
    }
}
