package com.cropdeal.userservice.controller;

import com.cropdeal.userservice.dto.*;
import com.cropdeal.userservice.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/profile")
@RequiredArgsConstructor
@Tag(name = "User Profiles", description = "Farmer, Dealer & Delivery Partner Profile Management")
public class UserProfileController {

    private final UserProfileService userProfileService;

    // --- Farmer Endpoints ---

    @GetMapping("/farmer/{userId}")
    @Operation(summary = "Get Farmer Profile with masked bank details")
    public ResponseEntity<FarmerProfileDto> getFarmerProfile(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userProfileService.getFarmerProfile(userId));
    }

    @PutMapping("/farmer/{userId}")
    @Operation(summary = "Update Farmer Profile details (Full name, phone, address, village, bank details)")
    public ResponseEntity<FarmerProfileDto> updateFarmerProfile(
            @PathVariable("userId") Long userId,
            @RequestBody FarmerProfileUpdateRequest request) {
        return ResponseEntity.ok(userProfileService.updateFarmerProfile(userId, request));
    }

    // --- Dealer Endpoints ---

    @GetMapping("/dealer/{userId}")
    @Operation(summary = "Get Dealer Profile with masked bank details")
    public ResponseEntity<DealerProfileDto> getDealerProfile(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userProfileService.getDealerProfile(userId));
    }

    @PutMapping("/dealer/{userId}")
    @Operation(summary = "Update Dealer Profile details (Business name, owner, phone, address, bank details)")
    public ResponseEntity<DealerProfileDto> updateDealerProfile(
            @PathVariable("userId") Long userId,
            @RequestBody DealerProfileUpdateRequest request) {
        return ResponseEntity.ok(userProfileService.updateDealerProfile(userId, request));
    }

    // --- Delivery Partner Endpoints ---

    @GetMapping("/delivery-partner/{userId}")
    @Operation(summary = "Get Delivery Partner Profile with vehicle and bank details")
    public ResponseEntity<DeliveryPartnerProfileDto> getDeliveryPartnerProfile(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userProfileService.getDeliveryPartnerProfile(userId));
    }

    @PutMapping("/delivery-partner/{userId}")
    @Operation(summary = "Update Delivery Partner Profile details (Vehicle, driving license, availability, bank details)")
    public ResponseEntity<DeliveryPartnerProfileDto> updateDeliveryPartnerProfile(
            @PathVariable("userId") Long userId,
            @RequestBody DeliveryPartnerProfileUpdateRequest request) {
        return ResponseEntity.ok(userProfileService.updateDeliveryPartnerProfile(userId, request));
    }

    // --- Unified Profile Endpoints (Any Role) ---

    @GetMapping("/{userId}")
    @Operation(summary = "Get Profile for any User ID (Auto-detects Farmer, Dealer, or Delivery Partner)")
    public ResponseEntity<Object> getUnifiedProfile(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userProfileService.getUnifiedProfile(userId));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update Profile for any User ID (Supports Farmer, Dealer, and Delivery Partner)")
    public ResponseEntity<Object> updateUnifiedProfile(
            @PathVariable("userId") Long userId,
            @RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(userProfileService.updateUnifiedProfile(userId, request));
    }
}