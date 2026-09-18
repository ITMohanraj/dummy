package com.cropdeal.notificationservice;

import com.cropdeal.notificationservice.controller.NotificationController;
import com.cropdeal.notificationservice.entity.NotificationRecord;
import com.cropdeal.notificationservice.service.NotificationService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Test
    void testGetUserNotifications_Success() throws Exception {
        NotificationRecord record = NotificationRecord.builder()
                .id(1L)
                .recipientUserId(101L)
                .title("Order Confirmed")
                .message("Your order #10 was confirmed.")
                .isRead(false)
                .build();

        Mockito.when(notificationService.getUserNotifications(101L)).thenReturn(List.of(record));

        mockMvc.perform(get("/api/v1/notifications/user/101"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$[0].title").value("Order Confirmed"));
    }

    @Test
    void testGetUnreadCount_Success() throws Exception {
        Mockito.when(notificationService.getUnreadCount(101L)).thenReturn(5L);

        mockMvc.perform(get("/api/v1/notifications/user/101/unread-count"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.unreadCount").value(5));
    }
}
