package com.cropdeal.cropservice.controller;

import com.cropdeal.cropservice.command.CreateCropCommand;
import com.cropdeal.cropservice.command.CropCommandHandler;
import com.cropdeal.cropservice.command.RestockCropCommand;
import com.cropdeal.cropservice.dto.CropCreateRequest;
import com.cropdeal.cropservice.dto.CropResponse;
import com.cropdeal.cropservice.query.CropQueryHandler;
import com.cropdeal.cropservice.query.GetAllCropsQuery;
import com.cropdeal.cropservice.query.GetFarmerCropsQuery;
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
@Tag(name = "Crop Listings & CQRS", description = "Farmer Crop Management & CQRS APIs")
public class CropController {

    private final CropCommandHandler commandHandler;
    private final CropQueryHandler queryHandler;

    // --- CQRS Commands (Mutations) ---

    @PostMapping
    @Operation(summary = "CQRS Command: Publish a new Crop listing by Farmer with Price validation")
    public ResponseEntity<CropResponse> createCrop(@Valid @RequestBody CropCreateRequest request) {
        CreateCropCommand command = new CreateCropCommand(request);
        CropResponse response = commandHandler.handle(command);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{cropId}/restock")
    @Operation(summary = "CQRS Command: Restock crop listing quantity")
    public ResponseEntity<CropResponse> restockCrop(
            @PathVariable("cropId") Long cropId,
            @RequestParam("addedQuantityKg") Double addedQuantityKg) {
        RestockCropCommand command = new RestockCropCommand(cropId, addedQuantityKg);
        return ResponseEntity.ok(commandHandler.handle(command));
    }

    // --- CQRS Queries (Read Projections) ---

    @GetMapping
    @Operation(summary = "CQRS Query: Get all crop listings")
    public ResponseEntity<List<CropResponse>> getAllCrops() {
        return ResponseEntity.ok(queryHandler.handle(new GetAllCropsQuery()));
    }

    @GetMapping("/all")
    @Operation(summary = "CQRS Query: Get all crop listings (alias)")
    public ResponseEntity<List<CropResponse>> getAllCropsAlias() {
        return ResponseEntity.ok(queryHandler.handle(new GetAllCropsQuery()));
    }

    @GetMapping("/farmer/{farmerId}")
    @Operation(summary = "CQRS Query: Get all crops posted by a specific farmer")
    public ResponseEntity<List<CropResponse>> getFarmerCrops(@PathVariable("farmerId") Long farmerId) {
        return ResponseEntity.ok(queryHandler.handle(new GetFarmerCropsQuery(farmerId)));
    }
}
