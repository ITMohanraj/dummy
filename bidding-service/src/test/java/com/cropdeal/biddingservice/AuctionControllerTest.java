package com.cropdeal.biddingservice;

import com.cropdeal.biddingservice.controller.AuctionController;
import com.cropdeal.biddingservice.dto.AuctionCreateRequest;
import com.cropdeal.biddingservice.entity.AuctionStatus;
import com.cropdeal.biddingservice.entity.CropAuction;
import com.cropdeal.biddingservice.service.BiddingService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuctionController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuctionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BiddingService biddingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateAuction_Success() throws Exception {
        AuctionCreateRequest req = AuctionCreateRequest.builder()
                .farmerId(10L)
                .cropId(101L)
                .quantityKg(200.0)
                .startingPricePerKg(BigDecimal.valueOf(30.0))
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusDays(2))
                .build();

        CropAuction auction = CropAuction.builder()
                .id(1L)
                .farmerId(10L)
                .cropName("Auction Onion")
                .status(AuctionStatus.LIVE)
                .build();

        Mockito.when(biddingService.createAuction(any(AuctionCreateRequest.class))).thenReturn(auction);

        mockMvc.perform(post("/api/v1/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testGetLiveAuctions_Success() throws Exception {
        CropAuction auction = CropAuction.builder().id(1L).status(AuctionStatus.LIVE).build();
        Mockito.when(biddingService.getLiveAuctions()).thenReturn(List.of(auction));

        mockMvc.perform(get("/api/v1/auctions/live"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$[0].id").value(1));
    }
}
