package com.cropdeal.cropservice;

import com.cropdeal.cropservice.controller.MarketplaceController;
import com.cropdeal.cropservice.dto.CropResponse;
import com.cropdeal.cropservice.entity.CropCategory;
import com.cropdeal.cropservice.entity.CropStatus;
import com.cropdeal.cropservice.query.CropQueryHandler;
import com.cropdeal.cropservice.query.FindNearbyCropsQuery;
import com.cropdeal.cropservice.query.GetCropByIdQuery;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MarketplaceController.class)
@AutoConfigureMockMvc(addFilters = false)
class MarketplaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CropQueryHandler queryHandler;

    @Test
    void testGetCropById_Success() throws Exception {
        CropResponse response = CropResponse.builder()
                .cropId(50L)
                .cropName("Wheat")
                .category(CropCategory.GRAIN)
                .pricePerKg(BigDecimal.valueOf(32.0))
                .status(CropStatus.ACTIVE)
                .build();

        Mockito.when(queryHandler.handle(any(GetCropByIdQuery.class))).thenReturn(response);

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

        Mockito.when(queryHandler.handle(any(FindNearbyCropsQuery.class))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/crops/nearby")
                        .param("latitude", "11.34")
                        .param("longitude", "77.71")
                        .param("radiusKm", "50.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cropName").value("Rice"));
    }
}
