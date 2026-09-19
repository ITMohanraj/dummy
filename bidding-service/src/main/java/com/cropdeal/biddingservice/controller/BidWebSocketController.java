package com.cropdeal.biddingservice.controller;

import com.cropdeal.biddingservice.dto.BidPlacementRequest;
import com.cropdeal.biddingservice.dto.WebSocketBidMessage;
import com.cropdeal.biddingservice.entity.AuctionBid;
import com.cropdeal.biddingservice.entity.CropAuction;
import com.cropdeal.biddingservice.repository.CropAuctionRepository;
import com.cropdeal.biddingservice.service.BiddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BidWebSocketController {

    private final BiddingService biddingService;
    private final CropAuctionRepository auctionRepo;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/bid.place")
    public void handleWebSocketBid(@Payload BidPlacementRequest request) {
        log.info("Received WebSocket bid from Dealer {} for Auction {}: ?{}/KG",
                request.getDealerId(), request.getAuctionId(), request.getBidPricePerKg());

        try {
            AuctionBid placedBid = biddingService.placeBid(request);
            CropAuction auction = auctionRepo.findById(request.getAuctionId()).orElse(null);

            WebSocketBidMessage broadcastMsg = WebSocketBidMessage.builder()
                    .type("BID_PLACED")
                    .auctionId(placedBid.getAuctionId())
                    .bidId(placedBid.getId())
                    .dealerId(placedBid.getDealerId())
                    .bidPricePerKg(placedBid.getBidPricePerKg())
                    .totalBidAmount(placedBid.getTotalBidAmount())
                    .currentHighestBid(auction != null ? auction.getCurrentHighestBid() : placedBid.getBidPricePerKg())
                    .winningDealerId(placedBid.getDealerId())
                    .auctionStatus(auction != null ? auction.getStatus().name() : "LIVE")
                    .timestamp(LocalDateTime.now())
                    .message("New highest bid placed successfully!")
                    .build();

            // Broadcast to specific auction channel and global live feed
            messagingTemplate.convertAndSend("/topic/auctions/" + request.getAuctionId() + "/bids", broadcastMsg);
            messagingTemplate.convertAndSend("/topic/auctions/live", broadcastMsg);

        } catch (Exception ex) {
            log.error("WebSocket bid failed: {}", ex.getMessage());
            WebSocketBidMessage errorMsg = WebSocketBidMessage.builder()
                    .type("BID_ERROR")
                    .auctionId(request.getAuctionId())
                    .dealerId(request.getDealerId())
                    .timestamp(LocalDateTime.now())
                    .message(ex.getMessage())
                    .build();

            messagingTemplate.convertAndSend("/queue/errors/" + request.getDealerId(), errorMsg);
        }
    }
}
