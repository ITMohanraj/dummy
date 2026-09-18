package com.cropdeal.reviewservice.controller;

import com.cropdeal.reviewservice.dto.FarmerReputationResponse;
import com.cropdeal.reviewservice.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews/reputation")
@RequiredArgsConstructor
@Tag(name = "Farmer Reputation", description = "Farmer Reputation & Average Rating Statistics")
public class FarmerReputationController {

    private final ReviewService reviewService;

    @GetMapping("/farmer/{farmerId}")
    @Operation(summary = "Get aggregated farmer reputation and rating breakdown")
    public ResponseEntity<FarmerReputationResponse> getReputation(@PathVariable("farmerId") Long farmerId) {
        return ResponseEntity.ok(reviewService.getFarmerReputation(farmerId));
    }
}
