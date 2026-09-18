package com.cropdeal.cropservice.controller;

import com.cropdeal.cropservice.dto.CropCreateRequest;
import com.cropdeal.cropservice.dto.CropResponse;
import com.cropdeal.cropservice.service.CropService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/crops")
@RequiredArgsConstructor
@Tag(name = "Crop Listings", description = "Farmer Crop Management APIs")
public class CropController {

    private final CropService cropService;

    @PostMapping
    @Operation(summary = "Publish a new Crop listing by Farmer with Price validation")
    public ResponseEntity<CropResponse> createCrop(@Valid @RequestBody CropCreateRequest request) {
        CropResponse response = cropService.createCrop(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all crop listings")
    public ResponseEntity<List<CropResponse>> getAllCrops() {
        return ResponseEntity.ok(cropService.getAllCrops());
    }

    @GetMapping("/all")
    @Operation(summary = "Get all crop listings (alias)")
    public ResponseEntity<List<CropResponse>> getAllCropsAlias() {
        return ResponseEntity.ok(cropService.getAllCrops());
    }

    @GetMapping("/farmer/{farmerId}")
    @Operation(summary = "Get all crops posted by a specific farmer")
    public ResponseEntity<List<CropResponse>> getFarmerCrops(@PathVariable("farmerId") Long farmerId) {
        return ResponseEntity.ok(cropService.getFarmerCrops(farmerId));
    }

    @PatchMapping("/{cropId}/restock")
    @Operation(summary = "Restock crop listing quantity")
    public ResponseEntity<CropResponse> restockCrop(
            @PathVariable("cropId") Long cropId,
            @RequestParam("addedQuantityKg") Double addedQuantityKg) {
        return ResponseEntity.ok(cropService.restockCrop(cropId, addedQuantityKg));
    }
}
