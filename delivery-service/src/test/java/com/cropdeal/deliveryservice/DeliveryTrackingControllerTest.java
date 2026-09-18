package com.cropdeal.deliveryservice;

import com.cropdeal.deliveryservice.controller.DeliveryTrackingController;
import com.cropdeal.deliveryservice.entity.DeliveryRequest;
import com.cropdeal.deliveryservice.entity.DeliveryStatus;
import com.cropdeal.deliveryservice.service.DeliveryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DeliveryTrackingController.class)
@AutoConfigureMockMvc(addFilters = false)
class DeliveryTrackingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeliveryService deliveryService;

    @Test
    void testUpdateStatus_Delivered() throws Exception {
        DeliveryRequest res = DeliveryRequest.builder().id(1L).status(DeliveryStatus.DELIVERED).build();
        Mockito.when(deliveryService.updateStatus(eq(1L), eq(DeliveryStatus.DELIVERED))).thenReturn(res);

        mockMvc.perform(patch("/api/v1/deliveries/tracking/1/status")
                        .param("status", "DELIVERED"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    void testUpdateStatus_InTransit() throws Exception {
        DeliveryRequest res = DeliveryRequest.builder().id(1L).status(DeliveryStatus.IN_TRANSIT).build();
        Mockito.when(deliveryService.updateStatus(eq(1L), eq(DeliveryStatus.IN_TRANSIT))).thenReturn(res);

        mockMvc.perform(patch("/api/v1/deliveries/tracking/1/status")
                        .param("status", "IN_TRANSIT"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"));
    }
}
