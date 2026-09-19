package com.cropdeal.cropservice.query;

import com.cropdeal.cropservice.entity.CropCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchMarketplaceQuery {
    private String cropName;
    private CropCategory category;
    private String state;
    private String district;
    private Boolean organic;
    private Pageable pageable;
}
