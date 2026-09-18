package com.cropdeal.deliveryservice.controller;

import com.cropdeal.deliveryservice.entity.DeliveryRequest;
import com.cropdeal.deliveryservice.entity.DeliveryStatus;
import com.cropdeal.deliveryservice.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/deliveries/tracking")
@RequiredArgsConstructor
@Tag(name = "Delivery Tracking", description = "Lifecycle Tracking & Escrow Release Triggers")
public class DeliveryTrackingController {

    private final DeliveryService deliveryService;

    @PatchMapping("/{deliveryId}/status")
    @Operation(summary = "Update delivery state (PICKED_UP, IN_TRANSIT, DELIVERED)")
    public ResponseEntity<DeliveryRequest> updateStatus(
            @PathVariable("deliveryId") Long deliveryId,
            @RequestParam("status") DeliveryStatus status) {
        return ResponseEntity.ok(deliveryService.updateStatus(deliveryId, status));
    }
}
