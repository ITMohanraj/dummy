package com.cropdeal.priceservice;

import com.cropdeal.priceservice.controller.PriceController;
import com.cropdeal.priceservice.dto.PriceValidationResponse;
import com.cropdeal.priceservice.service.PriceEngineService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PriceController.class)
@AutoConfigureMockMvc(addFilters = false)
class PriceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PriceEngineService priceService;

    @MockBean
    private com.cropdeal.priceservice.service.MandiSyncScheduler mandiSyncScheduler;

    @Test
    void testValidatePrice_Success() throws Exception {
        PriceValidationResponse resp = PriceValidationResponse.builder()
                .valid(true)
                .commodity("Onion")
                .referencePrice(BigDecimal.valueOf(35.0))
                .minAllowedPrice(BigDecimal.valueOf(29.75))
                .maxAllowedPrice(BigDecimal.valueOf(40.25))
                .unit("KG")
                .message("Farmer price is within allowed range")
                .build();

        Mockito.when(priceService.validateFarmerPrice(eq("Onion"), eq("Tamil Nadu"), eq("Erode"), eq("A"), eq(BigDecimal.valueOf(35.0))))
                .thenReturn(resp);

        mockMvc.perform(get("/api/v1/prices/validate")
                        .param("cropName", "Onion")
                        .param("state", "Tamil Nadu")
                        .param("district", "Erode")
                        .param("grade", "A")
                        .param("pricePerKg", "35.0"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.commodity").value("Onion"));
    }

    @Test
    void testValidatePrice_OutsideRange() throws Exception {
        PriceValidationResponse resp = PriceValidationResponse.builder()
                .valid(false)
                .commodity("Onion")
                .referencePrice(BigDecimal.valueOf(35.0))
                .minAllowedPrice(BigDecimal.valueOf(29.75))
                .maxAllowedPrice(BigDecimal.valueOf(40.25))
                .unit("KG")
                .message("Price out of bounds")
                .build();

        Mockito.when(priceService.validateFarmerPrice(eq("Onion"), eq("Tamil Nadu"), eq("Erode"), eq("A"), eq(BigDecimal.valueOf(60.0))))
                .thenReturn(resp);

        mockMvc.perform(get("/api/v1/prices/validate")
                        .param("cropName", "Onion")
                        .param("state", "Tamil Nadu")
                        .param("district", "Erode")
                        .param("grade", "A")
                        .param("pricePerKg", "60.0"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.valid").value(false));
    }
}
