package com.cropdeal.orderservice;

import com.cropdeal.orderservice.command.CreateOrderCommand;
import com.cropdeal.orderservice.command.OrderCommandHandler;
import com.cropdeal.orderservice.command.UpdateOrderStatusCommand;
import com.cropdeal.orderservice.controller.OrderController;
import com.cropdeal.orderservice.dto.OrderResponse;
import com.cropdeal.orderservice.dto.PurchaseRequest;
import com.cropdeal.orderservice.entity.OrderStatus;
import com.cropdeal.orderservice.query.OrderQueryHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderCommandHandler commandHandler;

    @MockBean
    private OrderQueryHandler queryHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testPurchaseCrop_Success() throws Exception {
        PurchaseRequest req = PurchaseRequest.builder()
                .dealerId(20L)
                .farmerId(10L)
                .cropId(101L)
                .quantityKg(100.0)
                .pricePerKg(BigDecimal.valueOf(35.0))
                .paymentMethod("WALLET")
                .build();

        OrderResponse res = OrderResponse.builder()
                .orderId(1L)
                .dealerId(20L)
                .farmerId(10L)
                .totalAmount(BigDecimal.valueOf(3500.0))
                .status(OrderStatus.CONFIRMED)
                .build();

        Mockito.when(commandHandler.handle(any(CreateOrderCommand.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/orders/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void testUpdateOrderStatus_Success() throws Exception {
        OrderResponse res = OrderResponse.builder()
                .orderId(1L)
                .status(OrderStatus.DELIVERED)
                .build();

        Mockito.when(commandHandler.handle(any(UpdateOrderStatusCommand.class))).thenReturn(res);

        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .param("status", "DELIVERED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }
}
