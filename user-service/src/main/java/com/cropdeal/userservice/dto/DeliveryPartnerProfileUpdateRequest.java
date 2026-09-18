package com.cropdeal.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartnerProfileUpdateRequest {
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String state;
    private String district;
    private Double latitude;
    private Double longitude;
    private String profileImageUrl;
    private String vehicleType;
    private String vehicleNumber;
    private String drivingLicense;
    private String availabilityStatus; // AVAILABLE, BUSY, OFFLINE
    private String bankAccountName;
    private String bankAccountNumber;
    private String ifscCode;
}