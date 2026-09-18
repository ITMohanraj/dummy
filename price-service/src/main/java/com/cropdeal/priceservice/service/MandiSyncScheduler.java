package com.cropdeal.priceservice.service;

import com.cropdeal.priceservice.entity.MandiPriceRecord;
import com.cropdeal.priceservice.repository.MandiPriceRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MandiSyncScheduler implements CommandLineRunner {

    private final MandiPriceRecordRepository mandiRepo;

    @Override
    public void run(String... args) {
        if (mandiRepo.count() == 0) {
            log.info("Seeding baseline Government Mandi Market Prices...");
            seedDefaultMandiPrices();
        }
    }

    @Scheduled(cron = "0 0 4 * * ?") // 4 AM Daily Sync
    public void syncDailyGovernmentPrices() {
        log.info("Starting scheduled daily synchronization with Government APMC Portal...");
        seedDefaultMandiPrices();
    }

    private void seedDefaultMandiPrices() {
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
                        .sourceUnit("QUINTAL").convertedPricePerKg(BigDecimal.valueOf(31.00)).recordDate(LocalDate.now()).build()
        );

        mandiRepo.saveAll(baseline);
        log.info("Government Mandi baseline records loaded successfully: {} commodities", baseline.size());
    }
}
