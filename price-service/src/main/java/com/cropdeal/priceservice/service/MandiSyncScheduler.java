package com.cropdeal.priceservice.service;

import com.cropdeal.priceservice.entity.MandiPriceRecord;
import com.cropdeal.priceservice.repository.MandiPriceRecordRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MandiSyncScheduler implements CommandLineRunner {

    private final MandiPriceRecordRepository mandiRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.builder().build();

    @Value("${cropdeal.agmarknet.api-url:https://api.data.gov.in/resource/9ef84268-d588-465a-a308-a864a43d0070}")
    private String agmarknetApiUrl;

    @Value("${cropdeal.agmarknet.api-key:579b464db66ec23bdd000001cdd3946e44ce4aad7209ff7b23ac571b}")
    private String agmarknetApiKey;

    @Value("${cropdeal.agmarknet.enabled:true}")
    private boolean agmarknetEnabled;

    @Value("${cropdeal.agmarknet.limit:1000}")
    private int fetchLimit;

    @Override
    public void run(String... args) {
        if (mandiRepo.count() == 0) {
            log.info("Initializing baseline Government Mandi Market Prices...");
            syncWithOfficialGovernmentPortal();
        }
    }

    @Scheduled(cron = "0 0 4 * * ?") // 4 AM Daily Scheduled Sync
    public void syncDailyGovernmentPrices() {
        log.info("Running scheduled 4 AM daily synchronization with Government APMC Portal...");
        syncWithOfficialGovernmentPortal();
    }

    public int syncWithOfficialGovernmentPortal() {
        if (!agmarknetEnabled || agmarknetApiKey == null || agmarknetApiKey.isBlank()) {
            log.warn("Official Government Agmarknet API key not configured or disabled. Using baseline reference data.");
            return seedDefaultMandiPrices();
        }

        List<MandiPriceRecord> syncedRecords = new ArrayList<>();
        int pageSize = 100;
        int totalToFetch = Math.max(fetchLimit, 100);

        for (int offset = 0; offset < totalToFetch; offset += pageSize) {
            try {
                int limit = Math.min(pageSize, totalToFetch - offset);
                String uri = String.format("%s?api-key=%s&format=json&offset=%d&limit=%d",
                        agmarknetApiUrl, agmarknetApiKey, offset, limit);

                log.info("Calling official Government Agmarknet Portal (offset={}, limit={}): {}", offset, limit, agmarknetApiUrl);
                String responseBody = restClient.get()
                        .uri(uri)
                        .retrieve()
                        .body(String.class);

                if (responseBody != null) {
                    JsonNode root = objectMapper.readTree(responseBody);
                    JsonNode recordsNode = root.has("records") ? root.get("records") : null;

                    if (recordsNode != null && recordsNode.isArray() && recordsNode.size() > 0) {
                        for (JsonNode item : recordsNode) {
                            try {
                                String commodity = item.has("commodity") ? item.get("commodity").asText() : "Unknown";
                                String state = item.has("state") ? item.get("state").asText() : "National";
                                String district = item.has("district") ? item.get("district").asText() : "General";
                                String market = item.has("market") ? item.get("market").asText() : "APMC Mandi";
                                String variety = item.has("variety") ? item.get("variety").asText() : "Standard";
                                String grade = item.has("grade") ? item.get("grade").asText() : "A";

                                BigDecimal minPrice = item.has("min_price") ? new BigDecimal(item.get("min_price").asText()) : BigDecimal.valueOf(2000);
                                BigDecimal maxPrice = item.has("max_price") ? new BigDecimal(item.get("max_price").asText()) : BigDecimal.valueOf(3000);
                                BigDecimal modalPrice = item.has("modal_price") ? new BigDecimal(item.get("modal_price").asText()) : BigDecimal.valueOf(2500);

                                // Mandi prices are in ₹/Quintal (100 KG) -> convert to ₹/KG
                                BigDecimal pricePerKg = modalPrice.divide(BigDecimal.valueOf(100.0), 2, RoundingMode.HALF_UP);

                                MandiPriceRecord record = MandiPriceRecord.builder()
                                        .commodity(commodity)
                                        .normalizedCommodity(commodity.toLowerCase().trim())
                                        .variety(variety)
                                        .grade(grade)
                                        .state(state)
                                        .district(district)
                                        .market(market)
                                        .minPrice(minPrice)
                                        .maxPrice(maxPrice)
                                        .modalPrice(modalPrice)
                                        .sourceUnit("QUINTAL")
                                        .convertedPricePerKg(pricePerKg)
                                        .recordDate(LocalDate.now())
                                        .build();

                                syncedRecords.add(record);
                            } catch (Exception parseEx) {
                                log.debug("Skipped unparseable record: {}", parseEx.getMessage());
                            }
                        }

                        if (recordsNode.size() < limit) {
                            // Reached end of available live records
                            break;
                        }
                    } else {
                        break;
                    }
                }
                // Rate limit breathing room
                Thread.sleep(250);

            } catch (Exception e) {
                log.warn("Government Agmarknet API batch offset={} encounter: {}. Stopping further pagination.", offset, e.getMessage());
                break;
            }
        }

        if (!syncedRecords.isEmpty()) {
            mandiRepo.saveAll(syncedRecords);
            log.info("Successfully synchronized {} live records from Government Agmarknet Portal!", syncedRecords.size());
            return syncedRecords.size();
        }

        // Automatic fallback if portal returned 0 records or encountered rate limits
        return seedDefaultMandiPrices();
    }

    public int seedDefaultMandiPrices() {
        List<MandiPriceRecord> baseline = List.of(
                MandiPriceRecord.builder().commodity("Onion").normalizedCommodity("onion").variety("Nasik Red").grade("A")
                        .state("Tamil Nadu").district("Erode").market("Erode Mandi")
                        .minPrice(BigDecimal.valueOf(3200)).modalPrice(BigDecimal.valueOf(3500)).maxPrice(BigDecimal.valueOf(3800))
                        .sourceUnit("QUINTAL").convertedPricePerKg(BigDecimal.valueOf(35.00)).recordDate(LocalDate.now()).build(),

                MandiPriceRecord.builder().commodity("Tomato").normalizedCommodity("tomato").variety("Hybrid").grade("A")
                        .state("Tamil Nadu").district("Salem").market("Salem Uzhavar Sandhai")
                        .minPrice(BigDecimal.valueOf(2000)).modalPrice(BigDecimal.valueOf(2400)).maxPrice(BigDecimal.valueOf(2800))
                        .sourceUnit("QUINTAL").convertedPricePerKg(BigDecimal.valueOf(24.00)).recordDate(LocalDate.now()).build(),

                MandiPriceRecord.builder().commodity("Potato").normalizedCommodity("potato").variety("Jyoti").grade("A")
                        .state("Tamil Nadu").district("Coimbatore").market("Coimbatore APMC")
                        .minPrice(BigDecimal.valueOf(2200)).modalPrice(BigDecimal.valueOf(2500)).maxPrice(BigDecimal.valueOf(2700))
                        .sourceUnit("QUINTAL").convertedPricePerKg(BigDecimal.valueOf(25.00)).recordDate(LocalDate.now()).build(),

                MandiPriceRecord.builder().commodity("Wheat").normalizedCommodity("wheat").variety("Sharbati").grade("A")
                        .state("Punjab").district("Ludhiana").market("Ludhiana Mandi")
                        .minPrice(BigDecimal.valueOf(2800)).modalPrice(BigDecimal.valueOf(3100)).maxPrice(BigDecimal.valueOf(3300))
                        .sourceUnit("QUINTAL").convertedPricePerKg(BigDecimal.valueOf(31.00)).recordDate(LocalDate.now()).build(),

                MandiPriceRecord.builder().commodity("Basmati Rice").normalizedCommodity("basmati rice").variety("Pusa 1121").grade("A")
                        .state("Haryana").district("Karnal").market("Karnal Grain Market")
                        .minPrice(BigDecimal.valueOf(5500)).modalPrice(BigDecimal.valueOf(6200)).maxPrice(BigDecimal.valueOf(6800))
                        .sourceUnit("QUINTAL").convertedPricePerKg(BigDecimal.valueOf(62.00)).recordDate(LocalDate.now()).build(),

                MandiPriceRecord.builder().commodity("Cotton").normalizedCommodity("cotton").variety("Medium Staple").grade("A")
                        .state("Gujarat").district("Rajkot").market("Rajkot APMC")
                        .minPrice(BigDecimal.valueOf(6800)).modalPrice(BigDecimal.valueOf(7300)).maxPrice(BigDecimal.valueOf(7800))
                        .sourceUnit("QUINTAL").convertedPricePerKg(BigDecimal.valueOf(73.00)).recordDate(LocalDate.now()).build(),

                MandiPriceRecord.builder().commodity("Turmeric").normalizedCommodity("turmeric").variety("Finger").grade("A")
                        .state("Tamil Nadu").district("Erode").market("Erode Regulated Market")
                        .minPrice(BigDecimal.valueOf(11000)).modalPrice(BigDecimal.valueOf(12500)).maxPrice(BigDecimal.valueOf(14000))
                        .sourceUnit("QUINTAL").convertedPricePerKg(BigDecimal.valueOf(125.00)).recordDate(LocalDate.now()).build(),

                MandiPriceRecord.builder().commodity("Red Chilli").normalizedCommodity("red chilli").variety("Guntur Sannam").grade("A")
                        .state("Andhra Pradesh").district("Guntur").market("Guntur Mirchi Yard")
                        .minPrice(BigDecimal.valueOf(16000)).modalPrice(BigDecimal.valueOf(18500)).maxPrice(BigDecimal.valueOf(21000))
                        .sourceUnit("QUINTAL").convertedPricePerKg(BigDecimal.valueOf(185.00)).recordDate(LocalDate.now()).build(),

                MandiPriceRecord.builder().commodity("Soybean").normalizedCommodity("soybean").variety("Yellow").grade("A")
                        .state("Madhya Pradesh").district("Indore").market("Indore Mandi")
                        .minPrice(BigDecimal.valueOf(4200)).modalPrice(BigDecimal.valueOf(4600)).maxPrice(BigDecimal.valueOf(4900))
                        .sourceUnit("QUINTAL").convertedPricePerKg(BigDecimal.valueOf(46.00)).recordDate(LocalDate.now()).build(),

                MandiPriceRecord.builder().commodity("Groundnut").normalizedCommodity("groundnut").variety("Bold").grade("A")
                        .state("Gujarat").district("Junagadh").market("Junagadh APMC")
                        .minPrice(BigDecimal.valueOf(5800)).modalPrice(BigDecimal.valueOf(6400)).maxPrice(BigDecimal.valueOf(6900))
                        .sourceUnit("QUINTAL").convertedPricePerKg(BigDecimal.valueOf(64.00)).recordDate(LocalDate.now()).build(),

                MandiPriceRecord.builder().commodity("Garlic").normalizedCommodity("garlic").variety("Desi").grade("A")
                        .state("Rajasthan").district("Kota").market("Kota Mandi")
                        .minPrice(BigDecimal.valueOf(12000)).modalPrice(BigDecimal.valueOf(14500)).maxPrice(BigDecimal.valueOf(17000))
                        .sourceUnit("QUINTAL").convertedPricePerKg(BigDecimal.valueOf(145.00)).recordDate(LocalDate.now()).build(),

                MandiPriceRecord.builder().commodity("Ginger").normalizedCommodity("ginger").variety("Fresh Green").grade("A")
                        .state("Kerala").district("Wayanad").market("Kalpetta APMC")
                        .minPrice(BigDecimal.valueOf(7500)).modalPrice(BigDecimal.valueOf(8800)).maxPrice(BigDecimal.valueOf(9800))
                        .sourceUnit("QUINTAL").convertedPricePerKg(BigDecimal.valueOf(88.00)).recordDate(LocalDate.now()).build(),

                MandiPriceRecord.builder().commodity("Sugarcane").normalizedCommodity("sugarcane").variety("CO 0238").grade("A")
                        .state("Uttar Pradesh").district("Muzaffarnagar").market("Muzaffarnagar Mandi")
                        .minPrice(BigDecimal.valueOf(350)).modalPrice(BigDecimal.valueOf(380)).maxPrice(BigDecimal.valueOf(400))
                        .sourceUnit("QUINTAL").convertedPricePerKg(BigDecimal.valueOf(3.80)).recordDate(LocalDate.now()).build()
        );

        mandiRepo.saveAll(baseline);
        log.info("Government Mandi baseline fallback records loaded: {} commodities", baseline.size());
        return baseline.size();
    }
}
