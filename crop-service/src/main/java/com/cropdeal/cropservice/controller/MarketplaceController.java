package com.cropdeal.cropservice.controller;

import com.cropdeal.cropservice.dto.CropResponse;
import com.cropdeal.cropservice.entity.CropCategory;
import com.cropdeal.cropservice.query.CropQueryHandler;
import com.cropdeal.cropservice.query.FindNearbyCropsQuery;
import com.cropdeal.cropservice.query.GetCropByIdQuery;
import com.cropdeal.cropservice.query.SearchMarketplaceQuery;
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
@Tag(name = "Marketplace & CQRS Queries", description = "Crop Discovery, Search & Read Projections")
public class MarketplaceController {

    private final CropQueryHandler queryHandler;

    @GetMapping("/search")
    @Operation(summary = "CQRS Query: Search active marketplace crop listings with filters and pagination")
    public ResponseEntity<Page<CropResponse>> searchCrops(
            @RequestParam(required = false) String cropName,
            @RequestParam(required = false) CropCategory category,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Boolean organic,
            Pageable pageable) {
        SearchMarketplaceQuery query = SearchMarketplaceQuery.builder()
                .cropName(cropName)
                .category(category)
                .state(state)
                .district(district)
                .organic(organic)
                .pageable(pageable)
                .build();
        return ResponseEntity.ok(queryHandler.handle(query));
    }

    @GetMapping("/nearby")
    @Operation(summary = "CQRS Query: Discover nearby active crops within a specified radius (KM)")
    public ResponseEntity<List<CropResponse>> getNearbyCrops(
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam(defaultValue = "50.0") Double radiusKm) {
        FindNearbyCropsQuery query = new FindNearbyCropsQuery(latitude, longitude, radiusKm);
        return ResponseEntity.ok(queryHandler.handle(query));
    }

    @GetMapping("/{cropId}")
    @Operation(summary = "CQRS Query: Get crop listing details by ID")
    public ResponseEntity<CropResponse> getCropById(@PathVariable("cropId") Long cropId) {
        return ResponseEntity.ok(queryHandler.handle(new GetCropByIdQuery(cropId)));
    }
}
