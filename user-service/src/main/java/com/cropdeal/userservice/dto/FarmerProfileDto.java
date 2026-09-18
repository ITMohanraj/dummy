package com.cropdeal.userservice.dto;

import com.cropdeal.userservice.entity.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmerProfileDto {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String state;
    private String district;
    private String village;
    private Double latitude;
    private Double longitude;
    private String profileImageUrl;
    private String maskedBankAccount;
    private String ifscCode;
    private VerificationStatus verificationStatus;
    private String accountStatus;
}
