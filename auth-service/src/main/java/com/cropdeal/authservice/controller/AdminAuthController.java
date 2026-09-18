package com.cropdeal.authservice.controller;

import com.cropdeal.authservice.dto.AdminCreateRequest;
import com.cropdeal.authservice.dto.AuthResponse;
import com.cropdeal.authservice.entity.AccountStatus;
import com.cropdeal.authservice.entity.UserAuth;
import com.cropdeal.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Authentication", description = "Admin User Management APIs")
public class AdminAuthController {

    private final AuthService authService;

    @PostMapping("/create-admin")
    @Operation(summary = "Create an additional Administrator account")
    public ResponseEntity<AuthResponse> createAdmin(@Valid @RequestBody AdminCreateRequest request) {
        AuthResponse response = authService.createAdmin(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/users/{userId}/status")
    @Operation(summary = "Activate, Inactivate, or Suspend a user account")
    public ResponseEntity<Map<String, Object>> updateUserStatus(
            @PathVariable("userId") Long userId,
            @RequestParam("status") AccountStatus status) {
        UserAuth user = authService.updateUserStatus(userId, status);
        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "email", user.getEmail(),
                "status", user.getStatus()
        ));
    }
}
