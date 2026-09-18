package com.cropdeal.paymentservice;

import com.cropdeal.paymentservice.controller.PaymentAdminController;
import com.cropdeal.paymentservice.entity.PaymentStatus;
import com.cropdeal.paymentservice.entity.PaymentTransaction;
import com.cropdeal.paymentservice.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentTransactionRepository paymentRepo;

    @Test
    void testGetAllTransactions_Success() throws Exception {
        PaymentTransaction txn = PaymentTransaction.builder()
                .id(1L)
                .transactionReference("PAY_1")
                .amount(BigDecimal.valueOf(500.0))
                .status(PaymentStatus.SUCCESS)
                .build();

        Mockito.when(paymentRepo.findAll()).thenReturn(List.of(txn));

        mockMvc.perform(get("/api/v1/payments/admin/all"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$[0].transactionReference").value("PAY_1"));
    }

    @Test
    void testGetById_Success() throws Exception {
        PaymentTransaction txn = PaymentTransaction.builder()
                .id(1L)
                .transactionReference("PAY_1")
                .status(PaymentStatus.SUCCESS)
                .build();

        Mockito.when(paymentRepo.findById(1L)).thenReturn(Optional.of(txn));

        mockMvc.perform(get("/api/v1/payments/admin/1"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.id").value(1));
    }
}
