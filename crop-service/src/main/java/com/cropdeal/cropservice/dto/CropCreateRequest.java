package com.cropdeal.cropservice.dto;

import com.cropdeal.cropservice.entity.CropCategory;
import com.cropdeal.cropservice.entity.QualityGrade;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropCreateRequest {

    @NotNull(message = "Farmer ID is required")
    private Long farmerId;

    @NotBlank(message = "Crop name is required")
    private String cropName;

    @NotNull(message = "Category is required")
    private CropCategory category;

    private String variety;
    private String grade; // A, B, C
    private QualityGrade quality;
    private boolean organic;
    private String description;

    @NotNull(message = "Quantity in KG is required")
    @Positive(message = "Quantity must be positive")
    private Double quantityKg;

    @NotNull(message = "Price per KG is required")
    @Positive(message = "Price per KG must be positive")
    private BigDecimal pricePerKg;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "District is required")
    private String district;

    private String location;
    private Double latitude;
    private Double longitude;
    private LocalDate harvestDate;
    private String imageUrl;
}
