package com.cropdeal.biddingservice;

import com.cropdeal.biddingservice.controller.BidController;
import com.cropdeal.biddingservice.dto.BidPlacementRequest;
import com.cropdeal.biddingservice.entity.AuctionBid;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BidController.class)
@AutoConfigureMockMvc(addFilters = false)
class BidControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BiddingService biddingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testPlaceBid_Success() throws Exception {
        BidPlacementRequest req = BidPlacementRequest.builder()
                .auctionId(1L)
                .dealerId(20L)
                .bidPricePerKg(BigDecimal.valueOf(35.0))
                .build();

        AuctionBid bid = AuctionBid.builder()
                .id(10L)
                .auctionId(1L)
                .dealerId(20L)
                .bidPricePerKg(BigDecimal.valueOf(35.0))
                .status("VALID")
                .build();

        Mockito.when(biddingService.placeBid(any(BidPlacementRequest.class))).thenReturn(bid);

        mockMvc.perform(post("/api/v1/bids/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                
                .andExpect(jsonPath("$.bidPricePerKg").value(35.0));
    }

    @Test
    void testGetBids_Success() throws Exception {
        AuctionBid bid = AuctionBid.builder().id(10L).auctionId(1L).build();
        Mockito.when(biddingService.getAuctionBids(eq(1L))).thenReturn(List.of(bid));

        mockMvc.perform(get("/api/v1/bids/auction/1"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$[0].id").value(10));
    }
}
