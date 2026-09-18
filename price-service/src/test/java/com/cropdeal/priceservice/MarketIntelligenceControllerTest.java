package com.cropdeal.priceservice;

import com.cropdeal.priceservice.controller.MarketIntelligenceController;
import com.cropdeal.priceservice.dto.MarketComparisonResponse;
import com.cropdeal.priceservice.dto.PriceTrendResponse;
import com.cropdeal.priceservice.service.PriceEngineService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MarketIntelligenceController.class)
@AutoConfigureMockMvc(addFilters = false)
class MarketIntelligenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PriceEngineService priceService;

    @Test
    void testCompareMarkets_Success() throws Exception {
        MarketComparisonResponse resp = MarketComparisonResponse.builder()
                .commodity("Onion")
                .overallMinPricePerKg(BigDecimal.valueOf(32.0))
                .overallMaxPricePerKg(BigDecimal.valueOf(38.0))
                .overallAvgPricePerKg(BigDecimal.valueOf(35.0))
                .markets(List.of())
                .build();

        Mockito.when(priceService.compareMarkets(eq("Onion"))).thenReturn(resp);

        mockMvc.perform(get("/api/v1/prices/intelligence/compare")
                        .param("commodity", "Onion"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.commodity").value("Onion"))
                .andExpect(jsonPath("$.overallAvgPricePerKg").value(35.0));
    }

    @Test
    void testGetPriceTrend_Success() throws Exception {
        PriceTrendResponse resp = PriceTrendResponse.builder()
                .commodity("Tomato")
                .trend("UP")
                .currentPrice(BigDecimal.valueOf(28.0))
                .previousPrice(BigDecimal.valueOf(24.0))
                .percentageChange(BigDecimal.valueOf(16.67))
                .build();

        Mockito.when(priceService.getPriceTrend(eq("Tomato"), eq(null), eq(null))).thenReturn(resp);

        mockMvc.perform(get("/api/v1/prices/intelligence/trend")
                        .param("commodity", "Tomato"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.trend").value("UP"));
    }
}
