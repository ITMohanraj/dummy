package com.cropdeal.auditservice;

import com.cropdeal.auditservice.controller.AuditLogController;
import com.cropdeal.auditservice.entity.AuditLog;
import com.cropdeal.auditservice.service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuditLogController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditLogService auditService;

    @Test
    void testGetAllLogs_Success() throws Exception {
        AuditLog entry = AuditLog.builder()
                .id(1L)
                .action("CROP_POSTED")
                .actorRole("FARMER")
                .entityId("101")
                .build();

        Mockito.when(auditService.getAllLogs()).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/v1/audit/logs"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$[0].action").value("CROP_POSTED"));
    }

    @Test
    void testGetLogsByAction_Success() throws Exception {
        AuditLog entry = AuditLog.builder()
                .id(1L)
                .action("ORDER_CREATED")
                .build();

        Mockito.when(auditService.getLogsByAction(eq("ORDER_CREATED"))).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/v1/audit/logs/action/ORDER_CREATED"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$[0].action").value("ORDER_CREATED"));
    }
}
