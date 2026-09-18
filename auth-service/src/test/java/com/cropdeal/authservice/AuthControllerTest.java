package com.cropdeal.authservice;

import com.cropdeal.authservice.controller.AuthController;
import com.cropdeal.authservice.dto.*;
import com.cropdeal.authservice.entity.Role;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

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
    void testRegisterFarmer_Success() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("farmer@cropdeal.com")
                .password("Password@123")
                .fullName("Ramesh Farmer")
                .phone("+91-9988776655")
                .role(Role.ROLE_FARMER)
                .build();

        AuthResponse response = AuthResponse.builder()
                .userId(1L)
                .email("farmer@cropdeal.com")
                .role(Role.ROLE_FARMER)
                .token("mock-jwt-token")
                .build();

        Mockito.when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("farmer@cropdeal.com"))
                .andExpect(jsonPath("$.role").value("ROLE_FARMER"));
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("farmer@cropdeal.com")
                .password("Password@123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .userId(1L)
                .email("farmer@cropdeal.com")
                .role(Role.ROLE_FARMER)
                .token("mock-jwt-token")
                .build();

        Mockito.when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }

    @Test
    void testForgotPassword_Success() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("farmer@cropdeal.com")
                .build();

        MessageResponse response = MessageResponse.builder()
                .message("If an account exists for this email, a password reset OTP has been sent.")
                .build();

        Mockito.when(authService.forgotPassword(any(ForgotPasswordRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If an account exists for this email, a password reset OTP has been sent."));
    }

    @Test
    void testVerifyOtp_Success() throws Exception {
        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .email("farmer@cropdeal.com")
                .otp("123456")
                .build();

        VerifyOtpResponse response = VerifyOtpResponse.builder()
                .message("OTP verified successfully")
                .resetToken("mock-reset-token-xyz")
                .build();

        Mockito.when(authService.verifyForgotPasswordOtp(any(VerifyOtpRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/forgot-password/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP verified successfully"))
                .andExpect(jsonPath("$.resetToken").value("mock-reset-token-xyz"));
    }

    @Test
    void testResetPassword_Success() throws Exception {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .resetToken("mock-reset-token-xyz")
                .newPassword("NewPassword@123")
                .confirmPassword("NewPassword@123")
                .build();

        MessageResponse response = MessageResponse.builder()
                .message("Password reset successfully")
                .build();

        Mockito.when(authService.resetPassword(any(ResetPasswordRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully"));
    }

    @Test
    void testChangePassword_Success() throws Exception {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("OldPassword@123")
                .newPassword("NewPassword@123")
                .confirmPassword("NewPassword@123")
                .build();

        MessageResponse response = MessageResponse.builder()
                .message("Password changed successfully")
                .build();

        Mockito.when(jwtUtils.getUserIdFromToken("mock-token")).thenReturn(1L);
        Mockito.when(authService.changePassword(eq(1L), any(ChangePasswordRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }

    @Test
    void testLogout_Success() throws Exception {
        MessageResponse response = MessageResponse.builder()
                .message("Logged out successfully")
                .build();

        Mockito.when(authService.logout(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }
}
