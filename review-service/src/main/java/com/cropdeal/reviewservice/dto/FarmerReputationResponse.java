package com.cropdeal.reviewservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmerReputationResponse {
    private Long farmerId;
    private Double averageRating;
    private Long totalReviewsCount;
    private Long fiveStarCount;
    private Long oneStarCount;
}
