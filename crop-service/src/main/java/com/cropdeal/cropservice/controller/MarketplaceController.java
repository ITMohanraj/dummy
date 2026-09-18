package com.cropdeal.cropservice.controller;

import com.cropdeal.cropservice.dto.CropResponse;
import com.cropdeal.cropservice.entity.CropCategory;
import com.cropdeal.cropservice.service.CropService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/crops")
@RequiredArgsConstructor
@Tag(name = "Marketplace", description = "Crop Discovery & Nearby Location APIs")
public class MarketplaceController {

    private final CropService cropService;

    @GetMapping("/search")
    @Operation(summary = "Search active marketplace crop listings with filters and pagination")
    public ResponseEntity<Page<CropResponse>> searchCrops(
            @RequestParam(required = false) String cropName,
            @RequestParam(required = false) CropCategory category,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Boolean organic,
            Pageable pageable) {
        return ResponseEntity.ok(cropService.searchMarketplace(cropName, category, state, district, organic, pageable));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Discover nearby active crops within a specified radius (KM)")
    public ResponseEntity<List<CropResponse>> getNearbyCrops(
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam(defaultValue = "50.0") Double radiusKm) {
        return ResponseEntity.ok(cropService.findNearbyCrops(latitude, longitude, radiusKm));
    }

    @GetMapping("/{cropId}")
    @Operation(summary = "Get crop listing details by ID")
    public ResponseEntity<CropResponse> getCropById(@PathVariable("cropId") Long cropId) {
        return ResponseEntity.ok(cropService.getCropById(cropId));
    }
}
