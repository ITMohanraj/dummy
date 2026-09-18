package com.cropdeal.biddingservice.controller;

import com.cropdeal.biddingservice.dto.AuctionCreateRequest;
import com.cropdeal.biddingservice.entity.CropAuction;
import com.cropdeal.biddingservice.service.BiddingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auctions")
@RequiredArgsConstructor
@Tag(name = "Crop Auctions", description = "Farmer Auction Creation & Winner Acceptance APIs")
public class AuctionController {

    private final BiddingService biddingService;

    @PostMapping
    @Operation(summary = "Create a new crop auction")
    public ResponseEntity<CropAuction> createAuction(@Valid @RequestBody AuctionCreateRequest request) {
        CropAuction auction = biddingService.createAuction(request);
        return new ResponseEntity<>(auction, HttpStatus.CREATED);
    }

    @GetMapping("/live")
    @Operation(summary = "Get all active live auctions")
    public ResponseEntity<List<CropAuction>> getLiveAuctions() {
        return ResponseEntity.ok(biddingService.getLiveAuctions());
    }

    @PostMapping("/{auctionId}/accept")
    @Operation(summary = "Farmer accepts highest winning bid")
    public ResponseEntity<CropAuction> acceptWinner(@PathVariable("auctionId") Long auctionId) {
        return ResponseEntity.ok(biddingService.acceptWinningBid(auctionId));
    }
}
