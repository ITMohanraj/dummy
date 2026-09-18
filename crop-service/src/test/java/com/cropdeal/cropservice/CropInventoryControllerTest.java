package com.cropdeal.cropservice;

import com.cropdeal.cropservice.controller.CropInventoryController;
import com.cropdeal.cropservice.dto.CropReservationRequest;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CropInventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CropInventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CropService cropService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testReserveCrop_Success() throws Exception {
        CropReservationRequest req = CropReservationRequest.builder()
                .cropId(10L)
                .quantityKg(100.0)
                .build();

        Mockito.doNothing().when(cropService).reserveQuantity(eq(10L), eq(100.0));

        mockMvc.perform(post("/api/v1/crops/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.status").value("RESERVED"));
    }

    @Test
    void testReleaseCrop_Success() throws Exception {
        CropReservationRequest req = CropReservationRequest.builder()
                .cropId(10L)
                .quantityKg(100.0)
                .build();

        Mockito.doNothing().when(cropService).releaseReservation(eq(10L), eq(100.0));

        mockMvc.perform(post("/api/v1/crops/inventory/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.status").value("RELEASED"));
    }
}
