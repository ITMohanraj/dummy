package com.cropdeal.biddingservice.controller;

import com.cropdeal.biddingservice.dto.BidPlacementRequest;
import com.cropdeal.biddingservice.entity.AuctionBid;
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
@RequestMapping("/api/v1/bids")
@RequiredArgsConstructor
@Tag(name = "Bidding", description = "Dealer Bid Placement & Auction Bid History APIs")
public class BidController {

    private final BiddingService biddingService;

    @PostMapping("/place")
    @Operation(summary = "Place a new incremental bid on a live auction")
    public ResponseEntity<AuctionBid> placeBid(@Valid @RequestBody BidPlacementRequest request) {
        AuctionBid bid = biddingService.placeBid(request);
        return new ResponseEntity<>(bid, HttpStatus.CREATED);
    }

    @GetMapping("/auction/{auctionId}")
    @Operation(summary = "Get all bids for an auction sorted by highest price")
    public ResponseEntity<List<AuctionBid>> getBids(@PathVariable("auctionId") Long auctionId) {
        return ResponseEntity.ok(biddingService.getAuctionBids(auctionId));
    }
}
