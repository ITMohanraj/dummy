package com.cropdeal.cropservice;

import com.cropdeal.cropservice.controller.MarketplaceController;
import com.cropdeal.cropservice.dto.CropResponse;
import com.cropdeal.cropservice.entity.CropCategory;
import com.cropdeal.cropservice.entity.CropStatus;
import com.cropdeal.cropservice.service.CropService;
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

@WebMvcTest(controllers = MarketplaceController.class)
@AutoConfigureMockMvc(addFilters = false)
class MarketplaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CropService cropService;

    @Test
    void testGetCropById_Success() throws Exception {
        CropResponse response = CropResponse.builder()
                .cropId(50L)
                .cropName("Wheat")
                .category(CropCategory.GRAIN)
                .pricePerKg(BigDecimal.valueOf(32.0))
                .status(CropStatus.ACTIVE)
                .build();

        Mockito.when(cropService.getCropById(50L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/crops/50"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.cropName").value("Wheat"));
    }

    @Test
    void testGetNearbyCrops_Success() throws Exception {
        CropResponse response = CropResponse.builder()
                .cropId(51L)
                .cropName("Rice")
                .latitude(11.3410)
                .longitude(77.7172)
                .build();

        Mockito.when(cropService.findNearbyCrops(eq(11.34), eq(77.71), eq(50.0))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/crops/nearby")
                        .param("latitude", "11.34")
                        .param("longitude", "77.71")
                        .param("radiusKm", "50.0"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$[0].cropName").value("Rice"));
    }
}
