package com.cropdeal.priceservice;

import com.cropdeal.priceservice.controller.PriceAlertController;
import com.cropdeal.priceservice.dto.PriceAlertRequest;
import com.cropdeal.priceservice.entity.PriceAlertSubscription;
import com.cropdeal.priceservice.service.PriceEngineService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PriceAlertController.class)
@AutoConfigureMockMvc(addFilters = false)
class PriceAlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PriceEngineService priceService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSubscribeAlert_Success() throws Exception {
        PriceAlertRequest req = PriceAlertRequest.builder()
                .userId(101L)
                .role("FARMER")
                .cropName("Tomato")
                .expectedMinPrice(BigDecimal.valueOf(20.0))
                .expectedMaxPrice(BigDecimal.valueOf(30.0))
                .build();

        PriceAlertSubscription sub = PriceAlertSubscription.builder()
                .id(1L)
                .userId(101L)
                .role("FARMER")
                .cropName("Tomato")
                .active(true)
                .build();

        Mockito.when(priceService.subscribeAlert(any(PriceAlertRequest.class))).thenReturn(sub);

        mockMvc.perform(post("/api/v1/prices/alerts/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                
                .andExpect(jsonPath("$.cropName").value("Tomato"));
    }

    @Test
    void testGetUserSubscriptions_Success() throws Exception {
        PriceAlertSubscription sub = PriceAlertSubscription.builder()
                .id(1L)
                .userId(101L)
                .cropName("Tomato")
                .build();

        Mockito.when(priceService.getUserSubscriptions(101L)).thenReturn(List.of(sub));

        mockMvc.perform(get("/api/v1/prices/alerts/user/101"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$[0].cropName").value("Tomato"));
    }
}
