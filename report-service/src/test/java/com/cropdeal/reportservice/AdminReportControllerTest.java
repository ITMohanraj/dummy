package com.cropdeal.reportservice;

import com.cropdeal.reportservice.controller.AdminReportController;
import com.cropdeal.reportservice.service.ReportService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Test
    void testGetSummary_Success() throws Exception {
        Mockito.when(reportService.getAdminPlatformSummary()).thenReturn(Map.of("totalUsers", 1420));

        mockMvc.perform(get("/api/v1/reports/admin/summary"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.totalUsers").value(1420));
    }

    @Test
    void testGetSummary_NotNull() throws Exception {
        Mockito.when(reportService.getAdminPlatformSummary()).thenReturn(Map.of("platformGrossMerchandiseValue", 1850000));

        mockMvc.perform(get("/api/v1/reports/admin/summary"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.platformGrossMerchandiseValue").value(1850000));
    }
}
