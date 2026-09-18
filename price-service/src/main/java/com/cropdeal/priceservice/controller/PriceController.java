package com.cropdeal.priceservice.controller;

import com.cropdeal.priceservice.dto.PriceValidationResponse;
import com.cropdeal.priceservice.service.PriceEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/prices")
@RequiredArgsConstructor
@Tag(name = "Government Prices", description = "Mandi Price Validation and Lookup APIs")
public class PriceController {

    private final PriceEngineService priceService;

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
}
