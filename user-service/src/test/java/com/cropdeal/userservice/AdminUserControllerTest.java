package com.cropdeal.userservice;

import com.cropdeal.userservice.controller.AdminUserController;
import com.cropdeal.userservice.dto.DealerProfileDto;
import com.cropdeal.userservice.dto.FarmerProfileDto;
import com.cropdeal.userservice.entity.VerificationStatus;
import com.cropdeal.userservice.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProfileService userProfileService;

    @Test
    void testVerifyUser_Success() throws Exception {
        Mockito.doNothing().when(userProfileService).updateVerificationStatus(101L, "FARMER", VerificationStatus.ADMIN_VERIFIED);

        mockMvc.perform(patch("/api/v1/users/admin/101/verify")
                        .param("role", "FARMER")
                        .param("status", "ADMIN_VERIFIED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("ADMIN_VERIFIED"));
    }

    @Test
    void testVerifyDealer_Success() throws Exception {
        Mockito.doNothing().when(userProfileService).updateVerificationStatus(202L, "DEALER", VerificationStatus.BANK_VERIFIED);

        mockMvc.perform(patch("/api/v1/users/admin/202/verify")
                        .param("role", "DEALER")
                        .param("status", "BANK_VERIFIED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("BANK_VERIFIED"));
    }

    @Test
    void testAdminGetAllUsers() throws Exception {
        Mockito.when(userProfileService.getAllUsers()).thenReturn(Map.of("totalUsers", 2));

        mockMvc.perform(get("/api/v1/users/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(2));
    }

    @Test
    void testAdminGetAllFarmers() throws Exception {
        FarmerProfileDto farmer = FarmerProfileDto.builder().userId(101L).fullName("Farmer Murugan").build();
        Mockito.when(userProfileService.getAllFarmers()).thenReturn(List.of(farmer));

        mockMvc.perform(get("/api/v1/users/admin/farmers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Farmer Murugan"));
    }
}