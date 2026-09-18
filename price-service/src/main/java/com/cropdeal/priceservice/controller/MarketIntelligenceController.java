package com.cropdeal.priceservice.controller;

import com.cropdeal.priceservice.dto.MarketComparisonResponse;
import com.cropdeal.priceservice.dto.PriceTrendResponse;
import com.cropdeal.priceservice.service.PriceEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/prices/intelligence")
@RequiredArgsConstructor
@Tag(name = "Market Intelligence", description = "APMC Comparison and Price Trend Indicators")
public class MarketIntelligenceController {

    private final PriceEngineService priceService;

    @GetMapping("/compare")
    @Operation(summary = "Compare market prices across various APMC mandis for a commodity")
    public ResponseEntity<MarketComparisonResponse> compareMarkets(@RequestParam("commodity") String commodity) {
        return ResponseEntity.ok(priceService.compareMarkets(commodity));
    }

    @GetMapping("/trend")
    @Operation(summary = "Get 30-day price trend indicator (UP, DOWN, STABLE)")
    public ResponseEntity<PriceTrendResponse> getPriceTrend(
            @RequestParam("commodity") String commodity,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String district) {
        return ResponseEntity.ok(priceService.getPriceTrend(commodity, state, district));
    }
}
