package com.cropdeal.reviewservice;

import com.cropdeal.reviewservice.controller.FarmerReputationController;
import com.cropdeal.reviewservice.dto.FarmerReputationResponse;
import com.cropdeal.reviewservice.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FarmerReputationController.class)
@AutoConfigureMockMvc(addFilters = false)
class FarmerReputationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Test
    void testGetReputation_Success() throws Exception {
        FarmerReputationResponse resp = FarmerReputationResponse.builder()
                .farmerId(10L)
                .averageRating(4.8)
                .totalReviewsCount(15L)
                .fiveStarCount(12L)
                .oneStarCount(0L)
                .build();

        Mockito.when(reviewService.getFarmerReputation(eq(10L))).thenReturn(resp);

        mockMvc.perform(get("/api/v1/reviews/reputation/farmer/10"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.averageRating").value(4.8))
                .andExpect(jsonPath("$.totalReviewsCount").value(15));
    }

    @Test
    void testGetReputation_Empty() throws Exception {
        FarmerReputationResponse resp = FarmerReputationResponse.builder()
                .farmerId(99L)
                .averageRating(0.0)
                .totalReviewsCount(0L)
                .build();

        Mockito.when(reviewService.getFarmerReputation(eq(99L))).thenReturn(resp);

        mockMvc.perform(get("/api/v1/reviews/reputation/farmer/99"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.averageRating").value(0.0));
    }
}
