package com.cropdeal.deliveryservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDeliveryRequestDto {
    @NotNull
    private Long orderId;
    @NotNull
    private Long dealerId;
    @NotNull
    private Long farmerId;
    @NotBlank
    private String pickupLocation;
    @NotBlank
    private String dropLocation;
    @NotNull
    @Positive
    private Double distanceKm;
}
