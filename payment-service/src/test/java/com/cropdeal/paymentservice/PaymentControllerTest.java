package com.cropdeal.paymentservice;

import com.cropdeal.paymentservice.controller.PaymentController;
import com.cropdeal.paymentservice.dto.PaymentRequest;
import com.cropdeal.paymentservice.dto.PaymentResponse;
import com.cropdeal.paymentservice.entity.PaymentStatus;
import com.cropdeal.paymentservice.service.PaymentService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testProcessPayment_Success() throws Exception {
        PaymentRequest req = PaymentRequest.builder()
                .idempotencyKey("KEY-12345")
                .orderId(10L)
                .payerUserId(20L)
                .amount(BigDecimal.valueOf(1500.0))
                .paymentMethod("UPI")
                .build();

        PaymentResponse res = PaymentResponse.builder()
                .paymentId(1L)
                .transactionReference("PAY_1234")
                .orderId(10L)
                .amount(BigDecimal.valueOf(1500.0))
                .status(PaymentStatus.SUCCESS)
                .build();

        Mockito.when(paymentService.processSimulatedPayment(any(PaymentRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/payments/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                
                .andExpect(jsonPath("$.transactionReference").value("PAY_1234"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void testGetByOrderId_Success() throws Exception {
        PaymentResponse res = PaymentResponse.builder()
                .paymentId(1L)
                .orderId(10L)
                .status(PaymentStatus.SUCCESS)
                .build();

        Mockito.when(paymentService.getPaymentByOrderId(eq(10L))).thenReturn(res);

        mockMvc.perform(get("/api/v1/payments/order/10"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.orderId").value(10));
    }
}
