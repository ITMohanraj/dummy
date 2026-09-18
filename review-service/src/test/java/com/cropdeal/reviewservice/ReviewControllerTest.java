package com.cropdeal.reviewservice;

import com.cropdeal.reviewservice.controller.ReviewController;
import com.cropdeal.reviewservice.dto.CreateReviewRequest;
import com.cropdeal.reviewservice.entity.FarmerReview;
import com.cropdeal.reviewservice.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testPostReview_Success() throws Exception {
        CreateReviewRequest req = CreateReviewRequest.builder()
                .orderId(10L)
                .dealerId(20L)
                .farmerId(10L)
                .rating(5)
                .comment("Excellent quality crop!")
                .build();

        FarmerReview res = FarmerReview.builder()
                .id(1L)
                .rating(5)
                .comment("Excellent quality crop!")
                .build();

        Mockito.when(reviewService.createReview(any(CreateReviewRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void testGetFarmerReviews_Success() throws Exception {
        FarmerReview res = FarmerReview.builder().id(1L).rating(5).build();
        Mockito.when(reviewService.getFarmerReviews(eq(10L))).thenReturn(List.of(res));

        mockMvc.perform(get("/api/v1/reviews/farmer/10"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$[0].rating").value(5));
    }
}
