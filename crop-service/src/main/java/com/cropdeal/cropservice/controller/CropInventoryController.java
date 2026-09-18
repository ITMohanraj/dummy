package com.cropdeal.cropservice.controller;

import com.cropdeal.cropservice.dto.CropReservationRequest;
import com.cropdeal.cropservice.service.CropService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/crops/inventory")
@RequiredArgsConstructor
@Tag(name = "Crop Inventory", description = "Internal Reservation & Release APIs for Purchase Saga")
public class CropInventoryController {

    private final CropService cropService;

    @PostMapping("/reserve")
    @Operation(summary = "Atomically reserve crop quantity during purchase")
    public ResponseEntity<Map<String, Object>> reserveCrop(@Valid @RequestBody CropReservationRequest request) {
        cropService.reserveQuantity(request.getCropId(), request.getQuantityKg());
        return ResponseEntity.ok(Map.of(
                "status", "RESERVED",
                "cropId", request.getCropId(),
                "quantityKg", request.getQuantityKg()
        ));
    }

    @PostMapping("/release")
    @Operation(summary = "Release reserved crop quantity on purchase compensation")
    public ResponseEntity<Map<String, Object>> releaseCrop(@Valid @RequestBody CropReservationRequest request) {
        cropService.releaseReservation(request.getCropId(), request.getQuantityKg());
        return ResponseEntity.ok(Map.of(
                "status", "RELEASED",
                "cropId", request.getCropId(),
                "quantityKg", request.getQuantityKg()
        ));
    }
}
