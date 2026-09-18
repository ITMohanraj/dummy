package com.cropdeal.reviewservice.service;

import com.cropdeal.reviewservice.dto.CreateReviewRequest;
import com.cropdeal.reviewservice.dto.FarmerReputationResponse;
import com.cropdeal.reviewservice.entity.FarmerReview;
import com.cropdeal.reviewservice.repository.FarmerReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final FarmerReviewRepository reviewRepo;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public FarmerReview createReview(CreateReviewRequest req) {
        if (reviewRepo.findByOrderIdAndDealerId(req.getOrderId(), req.getDealerId()).isPresent()) {
            throw new RuntimeException("Duplicate review: You have already reviewed this completed order.");
        }

        FarmerReview review = FarmerReview.builder()
                .orderId(req.getOrderId())
                .dealerId(req.getDealerId())
                .farmerId(req.getFarmerId())
                .rating(req.getRating())
                .comment(req.getComment())
                .build();

        FarmerReview saved = reviewRepo.save(review);

        try {
            rabbitTemplate.convertAndSend("review.exchange", "review.created", Map.of(
                    "reviewId", saved.getId(),
                    "farmerId", saved.getFarmerId(),
                    "dealerId", saved.getDealerId(),
                    "rating", saved.getRating()
            ));
        } catch (Exception e) {
            log.warn("Failed to publish review.created event: {}", e.getMessage());
        }

        return saved;
    }

    public List<FarmerReview> getFarmerReviews(Long farmerId) {
        return reviewRepo.findByFarmerIdOrderByCreatedAtDesc(farmerId);
    }

    public FarmerReputationResponse getFarmerReputation(Long farmerId) {
        List<FarmerReview> reviews = reviewRepo.findByFarmerIdOrderByCreatedAtDesc(farmerId);
        if (reviews.isEmpty()) {
            return FarmerReputationResponse.builder()
                    .farmerId(farmerId)
                    .averageRating(0.0)
                    .totalReviewsCount(0L)
                    .fiveStarCount(0L)
                    .oneStarCount(0L)
                    .build();
        }

        double avg = reviews.stream().mapToInt(FarmerReview::getRating).average().orElse(0.0);
        long five = reviews.stream().filter(r -> r.getRating() == 5).count();
        long one = reviews.stream().filter(r -> r.getRating() == 1).count();

        return FarmerReputationResponse.builder()
                .farmerId(farmerId)
                .averageRating(Math.round(avg * 10.0) / 10.0)
                .totalReviewsCount((long) reviews.size())
                .fiveStarCount(five)
                .oneStarCount(one)
                .build();
    }
}
