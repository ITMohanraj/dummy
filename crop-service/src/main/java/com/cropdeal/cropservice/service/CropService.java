package com.cropdeal.cropservice.service;

import com.cropdeal.cropservice.dto.*;
import com.cropdeal.cropservice.entity.CropCategory;
import com.cropdeal.cropservice.entity.CropListing;
import com.cropdeal.cropservice.entity.CropStatus;
import com.cropdeal.cropservice.exception.InsufficientCropQuantityException;
import com.cropdeal.cropservice.exception.InvalidPriceException;
import com.cropdeal.cropservice.exception.ResourceNotFoundException;
import com.cropdeal.cropservice.repository.CropListingRepository;
import com.cropdeal.cropservice.util.GeoDistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CropService {

    private final CropListingRepository cropRepo;
    private final RabbitTemplate rabbitTemplate;
    private final WebClient.Builder webClientBuilder;

    @Value("${cropdeal.price-service.url:http://price-service:8084}")
    private String priceServiceUrl;

    @Transactional
    public CropResponse createCrop(CropCreateRequest req) {
        // Price validation against price-service
        BigDecimal refGovPrice = validateFarmerPrice(req.getCropName(), req.getState(), req.getDistrict(), req.getGrade(), req.getPricePerKg());

        CropListing crop = CropListing.builder()
                .farmerId(req.getFarmerId())
                .cropName(req.getCropName())
                .category(req.getCategory())
                .variety(req.getVariety())
                .grade(req.getGrade())
                .quality(req.getQuality())
                .organic(req.isOrganic())
                .description(req.getDescription())
                .quantityKg(req.getQuantityKg())
                .availableQuantityKg(req.getQuantityKg())
                .pricePerKg(req.getPricePerKg())
                .referenceGovernmentPrice(refGovPrice)
                .state(req.getState())
                .district(req.getDistrict())
                .location(req.getLocation())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .harvestDate(req.getHarvestDate())
                .imageUrl(req.getImageUrl())
                .status(CropStatus.ACTIVE)
                .build();

        CropListing saved = cropRepo.save(crop);

        // Publish CROP_POSTED event
        try {
            rabbitTemplate.convertAndSend("crop.exchange", "crop.posted", Map.of(
                    "cropId", saved.getId(),
                    "cropName", saved.getCropName(),
                    "category", saved.getCategory().name(),
                    "pricePerKg", saved.getPricePerKg(),
                    "state", saved.getState(),
                    "district", saved.getDistrict()
            ));
        } catch (Exception e) {
            log.warn("Failed to publish crop.posted event: {}", e.getMessage());
        }

        return mapToResponse(saved);
    }

    private BigDecimal validateFarmerPrice(String cropName, String state, String district, String grade, BigDecimal pricePerKg) {
        try {
            PriceValidationResponse response = webClientBuilder.build()
                    .get()
                    .uri(priceServiceUrl + "/api/v1/prices/validate?cropName={crop}&state={state}&district={district}&grade={grade}&pricePerKg={price}",
                            cropName, state, district, grade != null ? grade : "A", pricePerKg)
                    .retrieve()
                    .bodyToMono(PriceValidationResponse.class)
                    .block();

            if (response != null && !response.isValid()) {
                throw new InvalidPriceException("Farmer price ₹" + pricePerKg + "/KG is outside allowed range: " + response.getMessage());
            }
            return response != null ? response.getReferencePrice() : BigDecimal.valueOf(30.0);
        } catch (InvalidPriceException ex) {
            throw ex;
        } catch (Exception e) {
            log.warn("Price service lookup fallback used: {}", e.getMessage());
            return BigDecimal.valueOf(35.0); // Fallback reference
        }
    }

    public Page<CropResponse> searchMarketplace(String cropName, CropCategory category, String state, String district, Boolean organic, Pageable pageable) {
        return cropRepo.searchMarketplace(cropName, category, state, district, organic, pageable)
                .map(this::mapToResponse);
    }

    public List<CropResponse> findNearbyCrops(Double latitude, Double longitude, Double radiusKm) {
        List<CropListing> activeCrops = cropRepo.findActiveWithCoordinates();
        return activeCrops.stream()
                .filter(crop -> {
                    double dist = GeoDistanceCalculator.calculateDistance(latitude, longitude, crop.getLatitude(), crop.getLongitude());
                    return dist <= radiusKm;
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CropResponse getCropById(Long cropId) {
        CropListing crop = cropRepo.findById(cropId)
                .orElseThrow(() -> new ResourceNotFoundException("Crop listing not found: " + cropId));
        return mapToResponse(crop);
    }

    public List<CropResponse> getAllCrops() {
        return cropRepo.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CropResponse> getFarmerCrops(Long farmerId) {
        return cropRepo.findByFarmerId(farmerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CropResponse restockCrop(Long cropId, Double addedQuantityKg) {
        CropListing crop = cropRepo.findById(cropId)
                .orElseThrow(() -> new ResourceNotFoundException("Crop listing not found: " + cropId));

        crop.setAvailableQuantityKg(crop.getAvailableQuantityKg() + addedQuantityKg);
        crop.setQuantityKg(crop.getQuantityKg() + addedQuantityKg);
        if (crop.getAvailableQuantityKg() > 0 && crop.getStatus() == CropStatus.SOLD_OUT) {
            crop.setStatus(CropStatus.ACTIVE);
        }
        return mapToResponse(cropRepo.save(crop));
    }

    @Transactional
    public void reserveQuantity(Long cropId, Double quantityKg) {
        CropListing crop = cropRepo.findById(cropId)
                .orElseThrow(() -> new ResourceNotFoundException("Crop listing not found: " + cropId));

        if (crop.getAvailableQuantityKg() < quantityKg) {
            throw new InsufficientCropQuantityException("Requested quantity " + quantityKg + " KG exceeds available " + crop.getAvailableQuantityKg() + " KG");
        }

        crop.setAvailableQuantityKg(crop.getAvailableQuantityKg() - quantityKg);
        if (crop.getAvailableQuantityKg() == 0) {
            crop.setStatus(CropStatus.SOLD_OUT);
        } else {
            crop.setStatus(CropStatus.PARTIALLY_SOLD);
        }
        cropRepo.save(crop);
    }

    @Transactional
    public void releaseReservation(Long cropId, Double quantityKg) {
        CropListing crop = cropRepo.findById(cropId)
                .orElseThrow(() -> new ResourceNotFoundException("Crop listing not found: " + cropId));

        crop.setAvailableQuantityKg(crop.getAvailableQuantityKg() + quantityKg);
        if (crop.getStatus() == CropStatus.SOLD_OUT && crop.getAvailableQuantityKg() > 0) {
            crop.setStatus(CropStatus.ACTIVE);
        }
        cropRepo.save(crop);
    }

    private CropResponse mapToResponse(CropListing crop) {
        BigDecimal total = crop.getPricePerKg().multiply(BigDecimal.valueOf(crop.getAvailableQuantityKg()));
        return CropResponse.builder()
                .cropId(crop.getId())
                .farmerId(crop.getFarmerId())
                .cropName(crop.getCropName())
                .category(crop.getCategory())
                .variety(crop.getVariety())
                .grade(crop.getGrade())
                .quality(crop.getQuality())
                .organic(crop.isOrganic())
                .description(crop.getDescription())
                .quantityKg(crop.getQuantityKg())
                .availableQuantityKg(crop.getAvailableQuantityKg())
                .pricePerKg(crop.getPricePerKg())
                .totalAmount(total)
                .referenceGovernmentPrice(crop.getReferenceGovernmentPrice())
                .state(crop.getState())
                .district(crop.getDistrict())
                .location(crop.getLocation())
                .latitude(crop.getLatitude())
                .longitude(crop.getLongitude())
                .imageUrl(crop.getImageUrl())
                .status(crop.getStatus())
                .createdAt(crop.getCreatedAt())
                .build();
    }
}
