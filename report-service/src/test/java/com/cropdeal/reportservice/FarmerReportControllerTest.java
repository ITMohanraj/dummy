package com.cropdeal.reportservice;

import com.cropdeal.reportservice.controller.FarmerReportController;
import com.cropdeal.reportservice.entity.FarmerReportProjection;
import com.cropdeal.reportservice.service.ReportService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FarmerReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class FarmerReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Test
    void testGetFarmerReport_Success() throws Exception {
        FarmerReportProjection proj = FarmerReportProjection.builder()
                .farmerId(10L)
                .totalRevenue(BigDecimal.valueOf(45000.0))
                .completedOrdersCount(8L)
                .build();

        Mockito.when(reportService.getFarmerReport(eq(10L))).thenReturn(proj);

        mockMvc.perform(get("/api/v1/reports/farmer/10"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.farmerId").value(10))
                .andExpect(jsonPath("$.totalRevenue").value(45000.0));
    }

    @Test
    void testGetFarmerReport_ZeroRevenue() throws Exception {
        FarmerReportProjection proj = FarmerReportProjection.builder()
                .farmerId(20L)
                .totalRevenue(BigDecimal.ZERO)
                .build();

        Mockito.when(reportService.getFarmerReport(eq(20L))).thenReturn(proj);

        mockMvc.perform(get("/api/v1/reports/farmer/20"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.totalRevenue").value(0));
    }
}
