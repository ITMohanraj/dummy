package com.cropdeal.negotiationservice;

import com.cropdeal.negotiationservice.controller.NegotiationController;
import com.cropdeal.negotiationservice.dto.CounterOfferRequest;
import com.cropdeal.negotiationservice.dto.NegotiationCreateRequest;
import com.cropdeal.negotiationservice.dto.NegotiationResponse;
import com.cropdeal.negotiationservice.entity.NegotiationStatus;
import com.cropdeal.negotiationservice.service.NegotiationService;
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

@WebMvcTest(controllers = NegotiationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NegotiationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NegotiationService negotiationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateNegotiation() throws Exception {
        NegotiationCreateRequest req = NegotiationCreateRequest.builder()
                .cropListingId(10L)
                .dealerId(20L)
                .farmerId(30L)
                .quantityKg(100.0)
                .offeredPricePerKg(new BigDecimal("25.00"))
                .build();

        NegotiationResponse res = NegotiationResponse.builder()
                .id(1L)
                .cropListingId(10L)
                .dealerId(20L)
                .farmerId(30L)
                .quantityKg(100.0)
                .currentOfferPricePerKg(new BigDecimal("25.00"))
                .status(NegotiationStatus.NEGOTIATING)
                .build();

        Mockito.when(negotiationService.initiateOrUpdateNegotiation(any(NegotiationCreateRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/negotiations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.currentOfferPricePerKg").value(25.00));
    }

    @Test
    void testCounterOffer() throws Exception {
        CounterOfferRequest req = CounterOfferRequest.builder()
                .counterPricePerKg(new BigDecimal("28.00"))
                .role("FARMER")
                .build();

        NegotiationResponse res = NegotiationResponse.builder()
                .id(1L)
                .currentOfferPricePerKg(new BigDecimal("28.00"))
                .status(NegotiationStatus.COUNTERED)
                .build();

        Mockito.when(negotiationService.counterOffer(eq(1L), any(CounterOfferRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/negotiations/1/counter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentOfferPricePerKg").value(28.00))
                .andExpect(jsonPath("$.status").value("COUNTERED"));
    }

    @Test
    void testAcceptOffer() throws Exception {
        NegotiationResponse res = NegotiationResponse.builder()
                .id(1L)
                .status(NegotiationStatus.ACCEPTED)
                .build();

        Mockito.when(negotiationService.acceptNegotiation(1L)).thenReturn(res);

        mockMvc.perform(post("/api/v1/negotiations/1/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }
}