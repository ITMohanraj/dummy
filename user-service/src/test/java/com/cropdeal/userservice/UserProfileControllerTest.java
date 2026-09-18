package com.cropdeal.userservice;

import com.cropdeal.userservice.controller.UserProfileController;
import com.cropdeal.userservice.dto.*;
import com.cropdeal.userservice.entity.VerificationStatus;
import com.cropdeal.userservice.service.UserProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProfileService userProfileService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetFarmerProfile_Success() throws Exception {
        FarmerProfileDto dto = FarmerProfileDto.builder()
                .userId(101L)
                .fullName("Murugan Farmer")
                .state("Tamil Nadu")
                .district("Erode")
                .maskedBankAccount("XXXXXX4321")
                .verificationStatus(VerificationStatus.PROFILE_VERIFIED)
                .build();

        Mockito.when(userProfileService.getFarmerProfile(101L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/users/profile/farmer/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Murugan Farmer"))
                .andExpect(jsonPath("$.maskedBankAccount").value("XXXXXX4321"));
    }

    @Test
    void testGetDealerProfile_Success() throws Exception {
        DealerProfileDto dto = DealerProfileDto.builder()
                .userId(202L)
                .businessName("Agri Traders Corp")
                .ownerName("Suresh Dealer")
                .build();

        Mockito.when(userProfileService.getDealerProfile(202L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/users/profile/dealer/202"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("Agri Traders Corp"));
    }

    @Test
    void testGetDeliveryPartnerProfile_WithBankDetails() throws Exception {
        DeliveryPartnerProfileDto dto = DeliveryPartnerProfileDto.builder()
                .userId(303L)
                .fullName("Ravi Driver")
                .vehicleType("MINI_TRUCK")
                .vehicleNumber("TN33AB1234")
                .maskedBankAccount("XXXXXX9876")
                .ifscCode("SBIN0001234")
                .bankAccountName("Ravi Kumar")
                .build();

        Mockito.when(userProfileService.getDeliveryPartnerProfile(303L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/users/profile/delivery-partner/303"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Ravi Driver"))
                .andExpect(jsonPath("$.maskedBankAccount").value("XXXXXX9876"))
                .andExpect(jsonPath("$.ifscCode").value("SBIN0001234"));
    }

    @Test
    void testUpdateDeliveryPartnerProfile_Success() throws Exception {
        DeliveryPartnerProfileUpdateRequest req = DeliveryPartnerProfileUpdateRequest.builder()
                .fullName("Ravi Kumar")
                .vehicleType("TRUCK_LARGE")
                .bankAccountName("Ravi Kumar")
                .bankAccountNumber("987654321012")
                .ifscCode("HDFC0005678")
                .build();

        DeliveryPartnerProfileDto dto = DeliveryPartnerProfileDto.builder()
                .userId(303L)
                .fullName("Ravi Kumar")
                .vehicleType("TRUCK_LARGE")
                .maskedBankAccount("XXXXXX1012")
                .ifscCode("HDFC0005678")
                .build();

        Mockito.when(userProfileService.updateDeliveryPartnerProfile(eq(303L), any(DeliveryPartnerProfileUpdateRequest.class))).thenReturn(dto);

        mockMvc.perform(put("/api/v1/users/profile/delivery-partner/303")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Ravi Kumar"))
                .andExpect(jsonPath("$.vehicleType").value("TRUCK_LARGE"));
    }

    @Test
    void testUpdateDealerProfile_Success() throws Exception {
        DealerProfileUpdateRequest req = DealerProfileUpdateRequest.builder()
                .businessName("Fresh Agro Traders")
                .ownerName("Suresh Dealer")
                .bankAccountName("Fresh Agro Traders")
                .bankAccountNumber("112233445566")
                .ifscCode("ICIC0001234")
                .build();

        DealerProfileDto dto = DealerProfileDto.builder()
                .userId(202L)
                .businessName("Fresh Agro Traders")
                .ownerName("Suresh Dealer")
                .maskedBankAccount("XXXXXX5566")
                .ifscCode("ICIC0001234")
                .build();

        Mockito.when(userProfileService.updateDealerProfile(eq(202L), any(DealerProfileUpdateRequest.class))).thenReturn(dto);

        mockMvc.perform(put("/api/v1/users/profile/dealer/202")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("Fresh Agro Traders"))
                .andExpect(jsonPath("$.maskedBankAccount").value("XXXXXX5566"));
    }
}