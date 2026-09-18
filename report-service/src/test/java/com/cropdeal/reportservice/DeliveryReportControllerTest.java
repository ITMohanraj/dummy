package com.cropdeal.reportservice;

import com.cropdeal.reportservice.controller.DeliveryReportController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DeliveryReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class DeliveryReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetPartnerReport_Success() throws Exception {
        mockMvc.perform(get("/api/v1/reports/delivery/partner/30"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.deliveryPartnerId").value(30))
                .andExpect(jsonPath("$.completedDeliveries").value(24));
    }

    @Test
    void testGetPartnerReport_Rating() throws Exception {
        mockMvc.perform(get("/api/v1/reports/delivery/partner/30"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.rating").value(4.9));
    }
}
