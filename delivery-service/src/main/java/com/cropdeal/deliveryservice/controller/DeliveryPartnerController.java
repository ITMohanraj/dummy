package com.cropdeal.deliveryservice.controller;

import com.cropdeal.deliveryservice.entity.DeliveryRequest;
import com.cropdeal.deliveryservice.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/deliveries/partner")
@RequiredArgsConstructor
@Tag(name = "Delivery Partner Marketplace", description = "Available Deliveries & Concurrency-safe Assignment APIs")
public class DeliveryPartnerController {

    private final DeliveryService deliveryService;

    @GetMapping("/available")
    @Operation(summary = "Get all available open delivery requests")
    public ResponseEntity<List<DeliveryRequest>> getAvailable() {
        return ResponseEntity.ok(deliveryService.getAvailableDeliveries());
    }

    @PostMapping("/{deliveryId}/accept")
    @Operation(summary = "Delivery Partner accepts delivery atomically")
    public ResponseEntity<DeliveryRequest> acceptDelivery(
            @PathVariable("deliveryId") Long deliveryId,
            @RequestParam("deliveryPartnerId") Long deliveryPartnerId) {
        return ResponseEntity.ok(deliveryService.acceptDelivery(deliveryId, deliveryPartnerId));
    }
}
