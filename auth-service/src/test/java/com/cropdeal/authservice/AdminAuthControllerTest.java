package com.cropdeal.authservice;

import com.cropdeal.authservice.controller.AdminAuthController;
import com.cropdeal.authservice.dto.AdminCreateRequest;
import com.cropdeal.authservice.dto.AuthResponse;
import com.cropdeal.authservice.entity.AccountStatus;
import com.cropdeal.authservice.entity.Role;
import com.cropdeal.authservice.entity.UserAuth;
import com.cropdeal.authservice.security.JwtAuthenticationFilter;
import com.cropdeal.authservice.security.JwtUtils;
import com.cropdeal.authservice.service.AuthService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateAdmin_Success() throws Exception {
        AdminCreateRequest request = AdminCreateRequest.builder()
                .email("admin2@cropdeal.com")
                .password("Admin@123")
                .fullName("Second Admin")
                .phone("+91-9123456780")
                .build();

        AuthResponse response = AuthResponse.builder()
                .userId(2L)
                .email("admin2@cropdeal.com")
                .role(Role.ROLE_ADMIN)
                .token("mock-admin-jwt")
                .build();

        Mockito.when(authService.createAdmin(any(AdminCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/admin/create-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("admin2@cropdeal.com"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    void testUpdateUserStatus_Success() throws Exception {
        UserAuth user = UserAuth.builder()
                .id(5L)
                .email("user5@cropdeal.com")
                .status(AccountStatus.SUSPENDED)
                .build();

        Mockito.when(authService.updateUserStatus(eq(5L), eq(AccountStatus.SUSPENDED))).thenReturn(user);

        mockMvc.perform(patch("/api/v1/auth/admin/users/5/status")
                        .param("status", "SUSPENDED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"))
                .andExpect(jsonPath("$.userId").value(5));
    }
}
