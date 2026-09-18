package com.cropdeal.authservice.controller;

import com.cropdeal.authservice.dto.*;
import com.cropdeal.authservice.entity.UserAuth;
import com.cropdeal.authservice.exception.UnauthorizedOperationException;
import com.cropdeal.authservice.security.JwtUtils;
import com.cropdeal.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication & Password Management", description = "Authentication, Registration, Password Management & Session Revocation APIs")
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    @Operation(summary = "Register Farmer, Dealer, or Delivery Partner")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/facebook")
    @Operation(summary = "OAuth2 Facebook Login or Registration")
    public ResponseEntity<AuthResponse> facebookLogin(@Valid @RequestBody FacebookLoginRequest request) {
        AuthResponse response = authService.loginWithFacebook(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate JWT Token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestParam("token") String token) {
        boolean valid = jwtUtils.validateToken(token);
        return ResponseEntity.ok(Map.of(
                "valid", valid,
                "claims", valid ? jwtUtils.getClaims(token) : Map.of()
        ));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Initiate Forgot Password - Generates & sends secure 6-digit OTP via Email/RabbitMQ")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        MessageResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password/verify-otp")
    @Operation(summary = "Verify Forgot Password OTP - Returns short-lived Password Reset Token")
    public ResponseEntity<VerifyOtpResponse> verifyForgotPasswordOtp(@Valid @RequestBody VerifyOtpRequest request) {
        VerifyOtpResponse response = authService.verifyForgotPasswordOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset Password using verified Reset Token")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        MessageResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    @Operation(
        summary = "Change Password for Authenticated User (Farmer, Dealer, Delivery Partner, Admin)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> changePassword(
            @AuthenticationPrincipal UserAuth currentUser,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody ChangePasswordRequest request) {

        Long userId;
        if (currentUser != null) {
            userId = currentUser.getId();
        } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            userId = jwtUtils.getUserIdFromToken(token);
        } else {
            throw new UnauthorizedOperationException("Full authentication is required to change password");
        }

        MessageResponse response = authService.changePassword(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(
        summary = "Logout & Revoke JWT Session (adds token hash to revocation blacklist)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        MessageResponse response = authService.logout(authHeader);
        return ResponseEntity.ok(response);
    }
}
