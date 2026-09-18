package com.cropdeal.walletservice;

import com.cropdeal.walletservice.controller.WalletController;
import com.cropdeal.walletservice.dto.TopUpRequest;
import com.cropdeal.walletservice.entity.UserWallet;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WalletController.class)
@AutoConfigureMockMvc(addFilters = false)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetWallet_Success() throws Exception {
        UserWallet wallet = UserWallet.builder()
                .id(1L)
                .userId(101L)
                .balance(BigDecimal.valueOf(5000.0))
                .currency("INR")
                .build();

        Mockito.when(walletService.getOrCreateWallet(eq(101L), any())).thenReturn(wallet);

        mockMvc.perform(get("/api/v1/wallets/user/101"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.userId").value(101))
                .andExpect(jsonPath("$.balance").value(5000.0));
    }

    @Test
    void testTopUp_Success() throws Exception {
        TopUpRequest req = TopUpRequest.builder().amount(BigDecimal.valueOf(2000.0)).build();
        UserWallet wallet = UserWallet.builder()
                .id(1L)
                .userId(101L)
                .balance(BigDecimal.valueOf(7000.0))
                .build();

        Mockito.when(walletService.topUpBalance(eq(101L), any(TopUpRequest.class))).thenReturn(wallet);

        mockMvc.perform(post("/api/v1/wallets/user/101/topup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(7000.0));
    }

    @Test
    void testDebit_Success() throws Exception {
        com.cropdeal.walletservice.dto.DebitRequest req = com.cropdeal.walletservice.dto.DebitRequest.builder()
                .amount(BigDecimal.valueOf(2000.0))
                .reason("WITHDRAW")
                .build();
        UserWallet wallet = UserWallet.builder()
                .id(1L)
                .userId(101L)
                .balance(BigDecimal.valueOf(5000.0))
                .build();

        Mockito.when(walletService.debitBalance(eq(101L), any(com.cropdeal.walletservice.dto.DebitRequest.class))).thenReturn(wallet);

        mockMvc.perform(post("/api/v1/wallets/user/101/debit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(5000.0));
    }
}
