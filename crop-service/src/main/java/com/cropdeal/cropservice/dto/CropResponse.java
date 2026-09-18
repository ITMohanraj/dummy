package com.cropdeal.cropservice.dto;

import com.cropdeal.cropservice.entity.CropCategory;
import com.cropdeal.cropservice.entity.CropStatus;
import com.cropdeal.cropservice.entity.QualityGrade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropResponse {
    private Long cropId;
    private Long farmerId;
    private String cropName;
    private CropCategory category;
    private String variety;
    private String grade;
    private QualityGrade quality;
    private boolean organic;
    private String description;
    private Double quantityKg;
    private Double availableQuantityKg;
    private BigDecimal pricePerKg;
    private BigDecimal totalAmount;
    private BigDecimal referenceGovernmentPrice;
    private String state;
    private String district;
    private String location;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private CropStatus status;
    private LocalDateTime createdAt;
}
