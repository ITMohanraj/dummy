package com.cropdeal.biddingservice.service;

import com.cropdeal.biddingservice.dto.AuctionCreateRequest;
import com.cropdeal.biddingservice.dto.BidPlacementRequest;
import com.cropdeal.biddingservice.entity.AuctionBid;
import com.cropdeal.biddingservice.entity.AuctionStatus;
import com.cropdeal.biddingservice.entity.CropAuction;
import com.cropdeal.biddingservice.repository.AuctionBidRepository;
import com.cropdeal.biddingservice.repository.CropAuctionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BiddingService {

    private final CropAuctionRepository auctionRepo;
    private final AuctionBidRepository bidRepo;
    private final RabbitTemplate rabbitTemplate;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Transactional
    public CropAuction createAuction(AuctionCreateRequest req) {
        CropAuction auction = CropAuction.builder()
                .farmerId(req.getFarmerId())
                .cropId(req.getCropId())
                .cropName(req.getCropName() != null ? req.getCropName() : "Auction Crop #" + req.getCropId())
                .quantityKg(req.getQuantityKg())
                .startingPricePerKg(req.getStartingPricePerKg())
                .minimumAcceptablePrice(req.getMinimumAcceptablePrice())
                .startDateTime(req.getStartDateTime())
                .endDateTime(req.getEndDateTime())
                .status(LocalDateTime.now().isBefore(req.getStartDateTime()) ? AuctionStatus.SCHEDULED : AuctionStatus.LIVE)
                .currentHighestBid(req.getStartingPricePerKg())
                .build();

        CropAuction saved = auctionRepo.save(auction);
        log.info("Created Crop Auction ID: {} for Crop: {}", saved.getId(), saved.getCropName());

        // Broadcast new auction over WebSocket
        try {
            com.cropdeal.biddingservice.dto.WebSocketBidMessage msg = com.cropdeal.biddingservice.dto.WebSocketBidMessage.builder()
                    .type("AUCTION_CREATED")
                    .auctionId(saved.getId())
                    .currentHighestBid(saved.getStartingPricePerKg())
                    .auctionStatus(saved.getStatus().name())
                    .timestamp(LocalDateTime.now())
                    .message("New crop auction created: " + saved.getCropName())
                    .build();
            messagingTemplate.convertAndSend("/topic/auctions/live", msg);
        } catch (Exception e) {
            log.debug("WebSocket broadcast failed: {}", e.getMessage());
        }

        return saved;
    }

    @Transactional
    public AuctionBid placeBid(BidPlacementRequest req) {
        CropAuction auction = auctionRepo.findById(req.getAuctionId())
                .orElseThrow(() -> new RuntimeException("Auction not found: " + req.getAuctionId()));

        LocalDateTime now = LocalDateTime.now();
        if (auction.getStatus() != AuctionStatus.LIVE || now.isAfter(auction.getEndDateTime()) || now.isBefore(auction.getStartDateTime())) {
            throw new RuntimeException("Auction is not live or bidding period has expired");
        }

        if (req.getBidPricePerKg().compareTo(auction.getCurrentHighestBid()) <= 0) {
            throw new RuntimeException("Bid price ₹" + req.getBidPricePerKg() + "/KG must be strictly higher than current highest bid ₹" + auction.getCurrentHighestBid());
        }

        BigDecimal total = req.getBidPricePerKg().multiply(BigDecimal.valueOf(auction.getQuantityKg()));

        AuctionBid bid = AuctionBid.builder()
                .auctionId(auction.getId())
                .dealerId(req.getDealerId())
                .bidPricePerKg(req.getBidPricePerKg())
                .totalBidAmount(total)
                .status("VALID")
                .build();

        AuctionBid savedBid = bidRepo.save(bid);

        // Update current highest bid atomically
        auction.setCurrentHighestBid(req.getBidPricePerKg());
        auction.setWinningDealerId(req.getDealerId());
        auctionRepo.save(auction);

        // Publish BID_PLACED event
        try {
            rabbitTemplate.convertAndSend("bidding.exchange", "bid.placed", Map.of(
                    "auctionId", auction.getId(),
                    "dealerId", req.getDealerId(),
                    "bidPricePerKg", req.getBidPricePerKg(),
                    "totalAmount", total
            ));
        } catch (Exception e) {
            log.warn("Failed to publish bid.placed event: {}", e.getMessage());
        }

        // Broadcast bid over WebSocket
        try {
            com.cropdeal.biddingservice.dto.WebSocketBidMessage broadcastMsg = com.cropdeal.biddingservice.dto.WebSocketBidMessage.builder()
                    .type("BID_PLACED")
                    .auctionId(savedBid.getAuctionId())
                    .bidId(savedBid.getId())
                    .dealerId(savedBid.getDealerId())
                    .bidPricePerKg(savedBid.getBidPricePerKg())
                    .totalBidAmount(savedBid.getTotalBidAmount())
                    .currentHighestBid(auction.getCurrentHighestBid())
                    .winningDealerId(auction.getWinningDealerId())
                    .auctionStatus(auction.getStatus().name())
                    .timestamp(LocalDateTime.now())
                    .message("New highest bid ₹" + savedBid.getBidPricePerKg() + "/KG placed!")
                    .build();

            messagingTemplate.convertAndSend("/topic/auctions/" + auction.getId() + "/bids", broadcastMsg);
            messagingTemplate.convertAndSend("/topic/auctions/live", broadcastMsg);
        } catch (Exception e) {
            log.debug("WebSocket broadcast failed: {}", e.getMessage());
        }

        return savedBid;
    }

    @Transactional
    public CropAuction acceptWinningBid(Long auctionId) {
        CropAuction auction = auctionRepo.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found: " + auctionId));

        auction.setStatus(AuctionStatus.ACCEPTED);
        CropAuction saved = auctionRepo.save(auction);

        // Publish BID_ACCEPTED event
        try {
            rabbitTemplate.convertAndSend("bidding.exchange", "bid.accepted", Map.of(
                    "auctionId", saved.getId(),
                    "farmerId", saved.getFarmerId(),
                    "winningDealerId", saved.getWinningDealerId(),
                    "highestBid", saved.getCurrentHighestBid(),
                    "quantityKg", saved.getQuantityKg()
            ));
        } catch (Exception e) {
            log.warn("Failed to publish bid.accepted event: {}", e.getMessage());
        }

        // Broadcast over WebSocket
        try {
            com.cropdeal.biddingservice.dto.WebSocketBidMessage broadcastMsg = com.cropdeal.biddingservice.dto.WebSocketBidMessage.builder()
                    .type("AUCTION_ACCEPTED")
                    .auctionId(saved.getId())
                    .currentHighestBid(saved.getCurrentHighestBid())
                    .winningDealerId(saved.getWinningDealerId())
                    .auctionStatus(saved.getStatus().name())
                    .timestamp(LocalDateTime.now())
                    .message("Winning bid accepted for auction #" + saved.getId())
                    .build();

            messagingTemplate.convertAndSend("/topic/auctions/" + saved.getId() + "/status", broadcastMsg);
            messagingTemplate.convertAndSend("/topic/auctions/live", broadcastMsg);
        } catch (Exception e) {
            log.debug("WebSocket broadcast failed: {}", e.getMessage());
        }

        return saved;
    }

    public List<CropAuction> getLiveAuctions() {
        return auctionRepo.findByStatus(AuctionStatus.LIVE);
    }

    public List<AuctionBid> getAuctionBids(Long auctionId) {
        return bidRepo.findByAuctionIdOrderByBidPricePerKgDesc(auctionId);
    }
}
