package com.cropdeal.reviewservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {
    @NotNull
    private Long orderId;
    @NotNull
    private Long dealerId;
    @NotNull
    private Long farmerId;
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;
    private String comment;
}
