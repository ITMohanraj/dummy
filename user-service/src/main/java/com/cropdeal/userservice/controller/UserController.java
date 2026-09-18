package com.cropdeal.userservice.controller;

import com.cropdeal.userservice.dto.DealerProfileDto;
import com.cropdeal.userservice.dto.DeliveryPartnerProfileDto;
import com.cropdeal.userservice.dto.FarmerProfileDto;
import com.cropdeal.userservice.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Directory", description = "Query all users and role-specific directories")
public class UserController {

    private final UserProfileService userProfileService;

    @GetMapping
    @Operation(summary = "Get all users or filter by role (FARMER, DEALER, DELIVERY_PARTNER)")
    public ResponseEntity<Object> getAllUsers(@RequestParam(name = "role", required = false) String role) {
        if (role == null || role.isBlank()) {
            return ResponseEntity.ok(userProfileService.getAllUsers());
        }

        String normalizedRole = role.trim().toUpperCase();
        if ("FARMER".equals(normalizedRole) || "ROLE_FARMER".equals(normalizedRole)) {
            return ResponseEntity.ok(userProfileService.getAllFarmers());
        } else if ("DEALER".equals(normalizedRole) || "ROLE_DEALER".equals(normalizedRole)) {
            return ResponseEntity.ok(userProfileService.getAllDealers());
        } else if ("DELIVERY_PARTNER".equals(normalizedRole) || "ROLE_DELIVERY_PARTNER".equals(normalizedRole)) {
            return ResponseEntity.ok(userProfileService.getAllDeliveryPartners());
        } else {
            return ResponseEntity.ok(userProfileService.getAllUsers());
        }
    }

    @GetMapping("/all")
    @Operation(summary = "Get all users summary categorized by role")
    public ResponseEntity<Map<String, Object>> getAllUsersSummary() {
        return ResponseEntity.ok(userProfileService.getAllUsers());
    }

    @GetMapping("/list")
    @Operation(summary = "Get flat list of all user profiles across all roles")
    public ResponseEntity<List<Object>> getAllUsersFlatList() {
        return ResponseEntity.ok(userProfileService.getAllUsersList());
    }

    @GetMapping("/farmers")
    @Operation(summary = "Get all Farmer profiles")
    public ResponseEntity<List<FarmerProfileDto>> getAllFarmers() {
        return ResponseEntity.ok(userProfileService.getAllFarmers());
    }

    @GetMapping("/dealers")
    @Operation(summary = "Get all Dealer profiles")
    public ResponseEntity<List<DealerProfileDto>> getAllDealers() {
        return ResponseEntity.ok(userProfileService.getAllDealers());
    }

    @GetMapping("/delivery-partners")
    @Operation(summary = "Get all Delivery Partner profiles")
    public ResponseEntity<List<DeliveryPartnerProfileDto>> getAllDeliveryPartners() {
        return ResponseEntity.ok(userProfileService.getAllDeliveryPartners());
    }
}