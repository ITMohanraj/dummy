package com.cropdeal.priceservice.service;

import com.cropdeal.priceservice.dto.*;
import com.cropdeal.priceservice.entity.MandiPriceRecord;
import com.cropdeal.priceservice.entity.PriceAlertSubscription;
import com.cropdeal.priceservice.repository.MandiPriceRecordRepository;
import com.cropdeal.priceservice.repository.PriceAlertSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceEngineService {

    private final MandiPriceRecordRepository mandiRepo;
    private final PriceAlertSubscriptionRepository alertRepo;
    private final RabbitTemplate rabbitTemplate;

    public PriceValidationResponse validateFarmerPrice(String cropName, String state, String district, String grade, BigDecimal pricePerKg) {
        List<MandiPriceRecord> records = mandiRepo.findMatchingPrices(cropName, state, district, grade);

        if (records.isEmpty()) {
            // Hierarchy fallback 2: state + district
            records = mandiRepo.findMatchingPrices(cropName, state, district, null);
        }
        if (records.isEmpty()) {
            // Hierarchy fallback 3: state
            records = mandiRepo.findMatchingPrices(cropName, state, null, null);
        }
        if (records.isEmpty()) {
            // Hierarchy fallback 4: commodity only
            records = mandiRepo.findByCommodityIgnoreCase(cropName);
        }

        if (records.isEmpty()) {
            return PriceValidationResponse.builder()
                    .valid(true) // Open when no gov data found, with warning
                    .commodity(cropName)
                    .referencePrice(pricePerKg)
                    .minAllowedPrice(pricePerKg.multiply(BigDecimal.valueOf(0.7)))
                    .maxAllowedPrice(pricePerKg.multiply(BigDecimal.valueOf(1.3)))
                    .unit("KG")
                    .message("No government reference price found. Farmer price accepted under unmonitored policy.")
                    .build();
        }

        MandiPriceRecord bestMatch = records.get(0);
        BigDecimal refPricePerKg = calculateGradeAdjustedPrice(bestMatch.getConvertedPricePerKg(), grade);

        BigDecimal minAllowed = refPricePerKg.multiply(BigDecimal.valueOf(0.85)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal maxAllowed = refPricePerKg.multiply(BigDecimal.valueOf(1.15)).setScale(2, RoundingMode.HALF_UP);

        boolean valid = pricePerKg.compareTo(minAllowed) >= 0 && pricePerKg.compareTo(maxAllowed) <= 0;

        return PriceValidationResponse.builder()
                .valid(valid)
                .commodity(cropName)
                .referencePrice(refPricePerKg)
                .minAllowedPrice(minAllowed)
                .maxAllowedPrice(maxAllowed)
                .unit("KG")
                .message(valid ? "Farmer price is within allowed range (±15% of government mandi reference)"
                               : "Price must be between ₹" + minAllowed + " and ₹" + maxAllowed + "/KG")
                .build();
    }

    private BigDecimal calculateGradeAdjustedPrice(BigDecimal basePrice, String targetGrade) {
        if (targetGrade == null || "A".equalsIgnoreCase(targetGrade)) {
            return basePrice;
        } else if ("B".equalsIgnoreCase(targetGrade)) {
            return basePrice.multiply(BigDecimal.valueOf(0.90)).setScale(2, RoundingMode.HALF_UP);
        } else if ("C".equalsIgnoreCase(targetGrade)) {
            return basePrice.multiply(BigDecimal.valueOf(0.80)).setScale(2, RoundingMode.HALF_UP);
        }
        return basePrice;
    }

    public MarketComparisonResponse compareMarkets(String commodity) {
        List<MandiPriceRecord> records = mandiRepo.findByCommodityIgnoreCase(commodity);
        if (records.isEmpty()) {
            return MarketComparisonResponse.builder().commodity(commodity).markets(List.of()).build();
        }

        List<MarketComparisonResponse.MarketPriceItem> items = records.stream()
                .map(r -> MarketComparisonResponse.MarketPriceItem.builder()
                        .market(r.getMarket())
                        .state(r.getState())
                        .district(r.getDistrict())
                        .minPrice(r.getMinPrice())
                        .modalPrice(r.getModalPrice())
                        .maxPrice(r.getMaxPrice())
                        .pricePerKg(r.getConvertedPricePerKg())
                        .build())
                .collect(Collectors.toList());

        BigDecimal minKg = items.stream().map(MarketComparisonResponse.MarketPriceItem::getPricePerKg).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal maxKg = items.stream().map(MarketComparisonResponse.MarketPriceItem::getPricePerKg).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        double avg = items.stream().mapToDouble(i -> i.getPricePerKg().doubleValue()).average().orElse(0.0);

        return MarketComparisonResponse.builder()
                .commodity(commodity)
                .overallMinPricePerKg(minKg)
                .overallMaxPricePerKg(maxKg)
                .overallAvgPricePerKg(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP))
                .markets(items)
                .build();
    }

    public PriceTrendResponse getPriceTrend(String commodity, String state, String district) {
        List<MandiPriceRecord> records = mandiRepo.findByCommodityIgnoreCaseAndRecordDateAfterOrderByRecordDateAsc(commodity, LocalDate.now().minusDays(30));
        if (records.size() < 2) {
            return PriceTrendResponse.builder()
                    .commodity(commodity)
                    .state(state)
                    .district(district)
                    .trend("STABLE")
                    .currentPrice(records.isEmpty() ? BigDecimal.ZERO : records.get(0).getConvertedPricePerKg())
                    .previousPrice(records.isEmpty() ? BigDecimal.ZERO : records.get(0).getConvertedPricePerKg())
                    .percentageChange(BigDecimal.ZERO)
                    .build();
        }

        MandiPriceRecord prev = records.get(0);
        MandiPriceRecord curr = records.get(records.size() - 1);
        BigDecimal diff = curr.getConvertedPricePerKg().subtract(prev.getConvertedPricePerKg());
        BigDecimal pct = diff.divide(prev.getConvertedPricePerKg(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        String trend = diff.compareTo(BigDecimal.ZERO) > 0 ? "UP" : (diff.compareTo(BigDecimal.ZERO) < 0 ? "DOWN" : "STABLE");

        return PriceTrendResponse.builder()
                .commodity(commodity)
                .state(state)
                .district(district)
                .trend(trend)
                .currentPrice(curr.getConvertedPricePerKg())
                .previousPrice(prev.getConvertedPricePerKg())
                .percentageChange(pct.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    public PriceAlertSubscription subscribeAlert(PriceAlertRequest req) {
        PriceAlertSubscription sub = PriceAlertSubscription.builder()
                .userId(req.getUserId())
                .role(req.getRole())
                .cropName(req.getCropName())
                .grade(req.getGrade())
                .state(req.getState())
                .district(req.getDistrict())
                .expectedMinPrice(req.getExpectedMinPrice())
                .expectedMaxPrice(req.getExpectedMaxPrice())
                .build();
        return alertRepo.save(sub);
    }

    public List<PriceAlertSubscription> getUserSubscriptions(Long userId) {
        return alertRepo.findByUserId(userId);
    }
}
