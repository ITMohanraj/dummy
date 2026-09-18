package com.cropdeal.deliveryservice;

import com.cropdeal.deliveryservice.controller.DeliveryRequestController;
import com.cropdeal.deliveryservice.dto.CreateDeliveryRequestDto;
import com.cropdeal.deliveryservice.entity.DeliveryRequest;
import com.cropdeal.deliveryservice.entity.DeliveryStatus;
import com.cropdeal.deliveryservice.service.DeliveryService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DeliveryRequestController.class)
@AutoConfigureMockMvc(addFilters = false)
class DeliveryRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeliveryService deliveryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateDelivery_Success() throws Exception {
        CreateDeliveryRequestDto dto = CreateDeliveryRequestDto.builder()
                .orderId(10L)
                .dealerId(20L)
                .farmerId(10L)
                .pickupLocation("Erode Farm")
                .dropLocation("Salem Market")
                .distanceKm(30.0)
                .build();

        DeliveryRequest res = DeliveryRequest.builder()
                .id(1L)
                .orderId(10L)
                .deliveryCharge(BigDecimal.valueOf(300.0))
                .status(DeliveryStatus.AVAILABLE)
                .build();

        Mockito.when(deliveryService.createDeliveryRequest(any(CreateDeliveryRequestDto.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/deliveries/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.deliveryCharge").value(300.0));
    }

    @Test
    void testGetDelivery_Success() throws Exception {
        DeliveryRequest res = DeliveryRequest.builder().id(1L).status(DeliveryStatus.AVAILABLE).build();
        Mockito.when(deliveryService.getDeliveryById(eq(1L))).thenReturn(res);

        mockMvc.perform(get("/api/v1/deliveries/requests/1"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.id").value(1));
    }
}
