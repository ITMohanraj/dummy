package com.cropdeal.priceservice.controller;

import com.cropdeal.priceservice.dto.PriceAlertRequest;
import com.cropdeal.priceservice.entity.PriceAlertSubscription;
import com.cropdeal.priceservice.service.PriceEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prices/alerts")
@RequiredArgsConstructor
@Tag(name = "Price Alerts", description = "Farmer & Dealer Price Subscriptions")
public class PriceAlertController {

    private final PriceEngineService priceService;

    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe to price alerts for a specific crop")
    public ResponseEntity<PriceAlertSubscription> subscribe(@Valid @RequestBody PriceAlertRequest request) {
        PriceAlertSubscription sub = priceService.subscribeAlert(request);
        return new ResponseEntity<>(sub, HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all price subscriptions for a user")
    public ResponseEntity<List<PriceAlertSubscription>> getUserSubscriptions(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(priceService.getUserSubscriptions(userId));
    }
}
