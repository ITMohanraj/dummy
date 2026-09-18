package com.cropdeal.walletservice;

import com.cropdeal.walletservice.controller.WalletTransactionController;
import com.cropdeal.walletservice.dto.EscrowHoldRequest;
import com.cropdeal.walletservice.dto.EscrowReleaseRequest;
import com.cropdeal.walletservice.entity.WalletTransaction;
import com.cropdeal.walletservice.service.WalletService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WalletTransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class WalletTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetTransactions_Success() throws Exception {
        WalletTransaction txn = WalletTransaction.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(500.0))
                .type("CREDIT")
                .build();

        Mockito.when(walletService.getTransactions(101L)).thenReturn(List.of(txn));

        mockMvc.perform(get("/api/v1/wallets/transactions/user/101"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$[0].type").value("CREDIT"));
    }

    @Test
    void testHoldEscrow_Success() throws Exception {
        EscrowHoldRequest req = EscrowHoldRequest.builder()
                .deliveryId(50L)
                .orderId(10L)
                .dealerId(20L)
                .amount(BigDecimal.valueOf(250.0))
                .build();

        Mockito.doNothing().when(walletService).holdDeliveryEscrow(any(EscrowHoldRequest.class));

        mockMvc.perform(post("/api/v1/wallets/transactions/escrow/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.status").value("HELD"));
    }
}
