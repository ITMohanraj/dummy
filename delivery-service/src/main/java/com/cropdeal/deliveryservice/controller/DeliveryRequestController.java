package com.cropdeal.deliveryservice.controller;

import com.cropdeal.deliveryservice.dto.CreateDeliveryRequestDto;
import com.cropdeal.deliveryservice.entity.DeliveryRequest;
import com.cropdeal.deliveryservice.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/deliveries/requests")
@RequiredArgsConstructor
@Tag(name = "Delivery Requests", description = "Dealer Delivery Creation & Lookup APIs")
public class DeliveryRequestController {

    private final DeliveryService deliveryService;

    @PostMapping
    @Operation(summary = "Dealer initiates delivery request with automated ₹10/km rate calculation")
    public ResponseEntity<DeliveryRequest> createDelivery(@Valid @RequestBody CreateDeliveryRequestDto dto) {
        DeliveryRequest res = deliveryService.createDeliveryRequest(dto);
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get delivery details by ID")
    public ResponseEntity<DeliveryRequest> getDelivery(@PathVariable("id") Long id) {
        return ResponseEntity.ok(deliveryService.getDeliveryById(id));
    }
}
