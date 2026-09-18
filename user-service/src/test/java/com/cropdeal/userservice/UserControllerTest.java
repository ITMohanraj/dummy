package com.cropdeal.userservice;

import com.cropdeal.userservice.controller.UserController;
import com.cropdeal.userservice.dto.DealerProfileDto;
import com.cropdeal.userservice.dto.DeliveryPartnerProfileDto;
import com.cropdeal.userservice.dto.FarmerProfileDto;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProfileService userProfileService;

    @Test
    void testGetAllUsers_Summary() throws Exception {
        FarmerProfileDto farmer = FarmerProfileDto.builder().userId(1L).fullName("Farmer One").build();
        DealerProfileDto dealer = DealerProfileDto.builder().userId(2L).businessName("Dealer One").build();
        DeliveryPartnerProfileDto dp = DeliveryPartnerProfileDto.builder().userId(3L).fullName("Driver One").build();

        Map<String, Object> summary = Map.of(
                "totalUsers", 3,
                "totalFarmers", 1,
                "totalDealers", 1,
                "totalDeliveryPartners", 1,
                "farmers", List.of(farmer),
                "dealers", List.of(dealer),
                "deliveryPartners", List.of(dp)
        );

        Mockito.when(userProfileService.getAllUsers()).thenReturn(summary);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(3))
                .andExpect(jsonPath("$.totalFarmers").value(1))
                .andExpect(jsonPath("$.farmers[0].fullName").value("Farmer One"));
    }

    @Test
    void testGetAllFarmers() throws Exception {
        FarmerProfileDto farmer = FarmerProfileDto.builder().userId(1L).fullName("Farmer Murugan").build();
        Mockito.when(userProfileService.getAllFarmers()).thenReturn(List.of(farmer));

        mockMvc.perform(get("/api/v1/users/farmers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Farmer Murugan"));
    }

    @Test
    void testGetAllDealers() throws Exception {
        DealerProfileDto dealer = DealerProfileDto.builder().userId(2L).businessName("Agri Mart").build();
        Mockito.when(userProfileService.getAllDealers()).thenReturn(List.of(dealer));

        mockMvc.perform(get("/api/v1/users/dealers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].businessName").value("Agri Mart"));
    }

    @Test
    void testGetAllDeliveryPartners() throws Exception {
        DeliveryPartnerProfileDto dp = DeliveryPartnerProfileDto.builder().userId(3L).fullName("Ravi Transporter").build();
        Mockito.when(userProfileService.getAllDeliveryPartners()).thenReturn(List.of(dp));

        mockMvc.perform(get("/api/v1/users/delivery-partners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Ravi Transporter"));
    }

    @Test
    void testGetUsersByRoleParam_Farmer() throws Exception {
        FarmerProfileDto farmer = FarmerProfileDto.builder().userId(1L).fullName("Farmer Murugan").build();
        Mockito.when(userProfileService.getAllFarmers()).thenReturn(List.of(farmer));

        mockMvc.perform(get("/api/v1/users?role=FARMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Farmer Murugan"));
    }
}