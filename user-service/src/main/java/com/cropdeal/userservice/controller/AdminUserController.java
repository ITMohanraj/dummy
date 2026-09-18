package com.cropdeal.userservice.controller;

import com.cropdeal.userservice.dto.DealerProfileDto;
import com.cropdeal.userservice.dto.DeliveryPartnerProfileDto;
import com.cropdeal.userservice.dto.FarmerProfileDto;
import com.cropdeal.userservice.entity.VerificationStatus;
import com.cropdeal.userservice.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/admin")
@RequiredArgsConstructor
@Tag(name = "Admin User Management", description = "Verification, KYC Approval & Directory APIs")
public class AdminUserController {

    private final UserProfileService userProfileService;

    @GetMapping("/all")
    @Operation(summary = "Admin: Get all users categorized by role with total counts")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        return ResponseEntity.ok(userProfileService.getAllUsers());
    }

    @GetMapping("/farmers")
    @Operation(summary = "Admin: Get all farmer profiles")
    public ResponseEntity<List<FarmerProfileDto>> getAllFarmers() {
        return ResponseEntity.ok(userProfileService.getAllFarmers());
    }

    @GetMapping("/dealers")
    @Operation(summary = "Admin: Get all dealer profiles")
    public ResponseEntity<List<DealerProfileDto>> getAllDealers() {
        return ResponseEntity.ok(userProfileService.getAllDealers());
    }

    @GetMapping("/delivery-partners")
    @Operation(summary = "Admin: Get all delivery partner profiles")
    public ResponseEntity<List<DeliveryPartnerProfileDto>> getAllDeliveryPartners() {
        return ResponseEntity.ok(userProfileService.getAllDeliveryPartners());
    }

    @PatchMapping("/{userId}/verify")
    @Operation(summary = "Update user verification status (KYC)")
    public ResponseEntity<Map<String, Object>> updateVerification(
            @PathVariable("userId") Long userId,
            @RequestParam("role") String role,
            @RequestParam("status") VerificationStatus status) {
        userProfileService.updateVerificationStatus(userId, role, status);
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "role", role,
                "verificationStatus", status,
                "message", "User verification status updated successfully"
        ));
    }
}