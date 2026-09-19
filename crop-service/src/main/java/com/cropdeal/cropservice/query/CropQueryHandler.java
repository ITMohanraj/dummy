package com.cropdeal.cropservice.query;

import com.cropdeal.cropservice.dto.CropResponse;
import com.cropdeal.cropservice.service.CropService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CropQueryHandler {

    private final CropService cropService;

    public CropResponse handle(GetCropByIdQuery query) {
        log.info("CQRS QueryHandler: Executing GetCropByIdQuery for cropId: {}", query.getCropId());
        return cropService.getCropById(query.getCropId());
    }

    public List<CropResponse> handle(GetFarmerCropsQuery query) {
        log.info("CQRS QueryHandler: Executing GetFarmerCropsQuery for farmerId: {}", query.getFarmerId());
        return cropService.getFarmerCrops(query.getFarmerId());
    }

    public List<CropResponse> handle(GetAllCropsQuery query) {
        log.info("CQRS QueryHandler: Executing GetAllCropsQuery");
        return cropService.getAllCrops();
    }

    public Page<CropResponse> handle(SearchMarketplaceQuery query) {
        log.info("CQRS QueryHandler: Executing SearchMarketplaceQuery for crop: {}, state: {}, district: {}",
                query.getCropName(), query.getState(), query.getDistrict());
        return cropService.searchMarketplace(query.getCropName(), query.getCategory(),
                query.getState(), query.getDistrict(), query.getOrganic(), query.getPageable());
    }

    public List<CropResponse> handle(FindNearbyCropsQuery query) {
        log.info("CQRS QueryHandler: Executing FindNearbyCropsQuery for lat: {}, lng: {}, radius: {} km",
                query.getLatitude(), query.getLongitude(), query.getRadiusKm());
        return cropService.findNearbyCrops(query.getLatitude(), query.getLongitude(), query.getRadiusKm());
    }
}
