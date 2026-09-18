package com.cropdeal.reviewservice.controller;

import com.cropdeal.reviewservice.dto.CreateReviewRequest;
import com.cropdeal.reviewservice.entity.FarmerReview;
import com.cropdeal.reviewservice.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Post-Order Farmer Reviews & Feedback APIs")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Dealer reviews Farmer after order completion (1-5 stars)")
    public ResponseEntity<FarmerReview> postReview(@Valid @RequestBody CreateReviewRequest request) {
        FarmerReview review = reviewService.createReview(request);
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }

    @GetMapping("/farmer/{farmerId}")
    @Operation(summary = "Get all reviews received by a farmer")
    public ResponseEntity<List<FarmerReview>> getFarmerReviews(@PathVariable("farmerId") Long farmerId) {
        return ResponseEntity.ok(reviewService.getFarmerReviews(farmerId));
    }
}
