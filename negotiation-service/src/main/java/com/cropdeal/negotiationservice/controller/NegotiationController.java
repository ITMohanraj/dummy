package com.cropdeal.negotiationservice.controller;

import com.cropdeal.negotiationservice.dto.CounterOfferRequest;
import com.cropdeal.negotiationservice.dto.NegotiationCreateRequest;
import com.cropdeal.negotiationservice.dto.NegotiationResponse;
import com.cropdeal.negotiationservice.service.NegotiationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/negotiations")
@RequiredArgsConstructor
@Tag(name = "Negotiation Service", description = "Multi-Dealer Independent Price Negotiation APIs")
public class NegotiationController {

    private final NegotiationService negotiationService;

    @PostMapping
    @Operation(summary = "Create or update price negotiation offer")
    public ResponseEntity<NegotiationResponse> createNegotiation(@Valid @RequestBody NegotiationCreateRequest request) {
        return new ResponseEntity<>(negotiationService.initiateOrUpdateNegotiation(request), HttpStatus.CREATED);
    }

    @PostMapping("/offer")
    @Operation(summary = "Dealer submits negotiation offer (Backward Compatible)")
    public ResponseEntity<NegotiationResponse> makeOffer(@Valid @RequestBody NegotiationCreateRequest request) {
        return new ResponseEntity<>(negotiationService.initiateOrUpdateNegotiation(request), HttpStatus.CREATED);
    }

    @GetMapping("/{negotiationId}")
    @Operation(summary = "Get negotiation details by ID")
    public ResponseEntity<NegotiationResponse> getNegotiation(@PathVariable("negotiationId") Long negotiationId) {
        return ResponseEntity.ok(negotiationService.getById(negotiationId));
    }

    @GetMapping("/crop/{cropId}")
    @Operation(summary = "List all negotiations for a crop listing")
    public ResponseEntity<List<NegotiationResponse>> getCropNegotiations(@PathVariable("cropId") Long cropId) {
        return ResponseEntity.ok(negotiationService.getCropNegotiations(cropId));
    }

    @GetMapping("/farmer/{farmerId}")
    @Operation(summary = "List all negotiations for a farmer")
    public ResponseEntity<List<NegotiationResponse>> getFarmerNegotiations(@PathVariable("farmerId") Long farmerId) {
        return ResponseEntity.ok(negotiationService.getFarmerNegotiations(farmerId));
    }

    @GetMapping("/dealer/{dealerId}")
    @Operation(summary = "List all negotiations for a dealer")
    public ResponseEntity<List<NegotiationResponse>> getDealerNegotiations(@PathVariable("dealerId") Long dealerId) {
        return ResponseEntity.ok(negotiationService.getDealerNegotiations(dealerId));
    }

    @PostMapping("/{negotiationId}/counter")
    @Operation(summary = "Farmer or Dealer submits a counter-offer")
    public ResponseEntity<NegotiationResponse> counterOffer(
            @PathVariable("negotiationId") Long negotiationId,
            @Valid @RequestBody CounterOfferRequest request) {
        return ResponseEntity.ok(negotiationService.counterOffer(negotiationId, request));
    }

    @PostMapping("/{negotiationId}/accept")
    @Operation(summary = "Farmer accepts dealer offer and triggers asynchronous order creation")
    public ResponseEntity<NegotiationResponse> acceptOffer(@PathVariable("negotiationId") Long negotiationId) {
        return ResponseEntity.ok(negotiationService.acceptNegotiation(negotiationId));
    }

    @PostMapping("/{negotiationId}/reject")
    @Operation(summary = "Farmer rejects dealer offer")
    public ResponseEntity<NegotiationResponse> rejectOffer(@PathVariable("negotiationId") Long negotiationId) {
        return ResponseEntity.ok(negotiationService.rejectNegotiation(negotiationId));
    }
}