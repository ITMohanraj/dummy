package com.cropdeal.cropservice;

import com.cropdeal.cropservice.controller.CropController;
import com.cropdeal.cropservice.dto.CropCreateRequest;
import com.cropdeal.cropservice.dto.CropResponse;
import com.cropdeal.cropservice.entity.CropCategory;
import com.cropdeal.cropservice.entity.CropStatus;
import com.cropdeal.cropservice.service.CropService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CropController.class)
@AutoConfigureMockMvc(addFilters = false)
class CropControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CropService cropService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateCrop_Success() throws Exception {
        CropCreateRequest request = CropCreateRequest.builder()
                .farmerId(10L)
                .cropName("Tomato")
                .category(CropCategory.VEGETABLE)
                .quantityKg(500.0)
                .pricePerKg(BigDecimal.valueOf(25.0))
                .state("Tamil Nadu")
                .district("Salem")
                .build();

        CropResponse response = CropResponse.builder()
                .cropId(101L)
                .cropName("Tomato")
                .category(CropCategory.VEGETABLE)
                .availableQuantityKg(500.0)
                .pricePerKg(BigDecimal.valueOf(25.0))
                .status(CropStatus.ACTIVE)
                .build();

        Mockito.when(cropService.createCrop(any(CropCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/crops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                
                .andExpect(jsonPath("$.cropName").value("Tomato"))
                .andExpect(jsonPath("$.cropId").value(101));
    }

    @Test
    void testRestockCrop_Success() throws Exception {
        CropResponse response = CropResponse.builder()
                .cropId(101L)
                .cropName("Tomato")
                .availableQuantityKg(800.0)
                .status(CropStatus.ACTIVE)
                .build();

        Mockito.when(cropService.restockCrop(eq(101L), eq(300.0))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/crops/101/restock")
                        .param("addedQuantityKg", "300.0"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.availableQuantityKg").value(800.0));
    }

    @Test
    void testGetAllCrops_Success() throws Exception {
        CropResponse response = CropResponse.builder()
                .cropId(101L)
                .cropName("Tomato")
                .availableQuantityKg(500.0)
                .status(CropStatus.ACTIVE)
                .build();

        Mockito.when(cropService.getAllCrops()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/crops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cropName").value("Tomato"))
                .andExpect(jsonPath("$[0].cropId").value(101));
    }
}
