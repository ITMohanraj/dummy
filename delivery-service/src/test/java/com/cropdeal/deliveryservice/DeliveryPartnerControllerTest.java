package com.cropdeal.deliveryservice;

import com.cropdeal.deliveryservice.controller.DeliveryPartnerController;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DeliveryPartnerController.class)
@AutoConfigureMockMvc(addFilters = false)
class DeliveryPartnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeliveryService deliveryService;

    @Test
    void testGetAvailableDeliveries_Success() throws Exception {
        DeliveryRequest res = DeliveryRequest.builder().id(1L).status(DeliveryStatus.AVAILABLE).build();
        Mockito.when(deliveryService.getAvailableDeliveries()).thenReturn(List.of(res));

        mockMvc.perform(get("/api/v1/deliveries/partner/available"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void testAcceptDelivery_Success() throws Exception {
        DeliveryRequest res = DeliveryRequest.builder().id(1L).deliveryPartnerId(30L).status(DeliveryStatus.ASSIGNED).build();
        Mockito.when(deliveryService.acceptDelivery(eq(1L), eq(30L))).thenReturn(res);

        mockMvc.perform(post("/api/v1/deliveries/partner/1/accept")
                        .param("deliveryPartnerId", "30"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.status").value("ASSIGNED"));
    }
}
