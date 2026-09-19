package com.cropdeal.cropservice.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindNearbyCropsQuery {
    private Double latitude;
    private Double longitude;
    private Double radiusKm;
}
