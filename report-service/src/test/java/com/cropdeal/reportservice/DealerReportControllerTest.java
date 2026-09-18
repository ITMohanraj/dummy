package com.cropdeal.reportservice;

import com.cropdeal.reportservice.controller.DealerReportController;
import com.cropdeal.reportservice.entity.DealerReportProjection;
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

@WebMvcTest(controllers = DealerReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class DealerReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Test
    void testGetDealerReport_Success() throws Exception {
        DealerReportProjection proj = DealerReportProjection.builder()
                .dealerId(20L)
                .totalPurchases(BigDecimal.valueOf(62000.0))
                .completedOrdersCount(11L)
                .build();

        Mockito.when(reportService.getDealerReport(eq(20L))).thenReturn(proj);

        mockMvc.perform(get("/api/v1/reports/dealer/20"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.dealerId").value(20))
                .andExpect(jsonPath("$.totalPurchases").value(62000.0));
    }

    @Test
    void testGetDealerReport_Expenses() throws Exception {
        DealerReportProjection proj = DealerReportProjection.builder()
                .dealerId(20L)
                .deliveryExpenses(BigDecimal.valueOf(3500.0))
                .build();

        Mockito.when(reportService.getDealerReport(eq(20L))).thenReturn(proj);

        mockMvc.perform(get("/api/v1/reports/dealer/20"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.deliveryExpenses").value(3500.0));
    }
}
