package com.cropdeal.userservice.service;

import com.cropdeal.userservice.dto.*;
import com.cropdeal.userservice.entity.DealerProfile;
import com.cropdeal.userservice.entity.DeliveryPartnerProfile;
import com.cropdeal.userservice.entity.FarmerProfile;
import com.cropdeal.userservice.entity.VerificationStatus;
import com.cropdeal.userservice.repository.DealerProfileRepository;
import com.cropdeal.userservice.repository.DeliveryPartnerProfileRepository;
import com.cropdeal.userservice.repository.FarmerProfileRepository;
import com.cropdeal.userservice.util.MaskingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final FarmerProfileRepository farmerRepo;
    private final DealerProfileRepository dealerRepo;
    private final DeliveryPartnerProfileRepository deliveryRepo;

    // --- Farmer Profile Methods ---

    public FarmerProfileDto getFarmerProfile(Long userId) {
        FarmerProfile profile = farmerRepo.findByUserId(userId)
                .orElseGet(() -> farmerRepo.save(FarmerProfile.builder()
                        .userId(userId)
                        .fullName("Farmer #" + userId)
                        .accountStatus("ACTIVE")
                        .verificationStatus(VerificationStatus.UNVERIFIED)
                        .build()));

        return mapToFarmerDto(profile);
    }

    @Transactional
    public FarmerProfileDto updateFarmerProfile(Long userId, FarmerProfileUpdateRequest req) {
        FarmerProfile profile = farmerRepo.findByUserId(userId)
                .orElseGet(() -> FarmerProfile.builder().userId(userId).accountStatus("ACTIVE").verificationStatus(VerificationStatus.UNVERIFIED).build());

        if (req.getFullName() != null) profile.setFullName(req.getFullName());
        if (req.getEmail() != null) profile.setEmail(req.getEmail());
        if (req.getPhone() != null) profile.setPhone(req.getPhone());
        if (req.getAddress() != null) profile.setAddress(req.getAddress());
        if (req.getState() != null) profile.setState(req.getState());
        if (req.getDistrict() != null) profile.setDistrict(req.getDistrict());
        if (req.getVillage() != null) profile.setVillage(req.getVillage());
        if (req.getLatitude() != null) profile.setLatitude(req.getLatitude());
        if (req.getLongitude() != null) profile.setLongitude(req.getLongitude());
        if (req.getProfileImageUrl() != null) profile.setProfileImageUrl(req.getProfileImageUrl());
        if (req.getBankAccountName() != null) profile.setBankAccountName(req.getBankAccountName());
        if (req.getBankAccountNumber() != null) profile.setBankAccountNumber(req.getBankAccountNumber());
        if (req.getIfscCode() != null) profile.setIfscCode(req.getIfscCode());

        FarmerProfile saved = farmerRepo.save(profile);
        log.info("Updated Farmer profile for userId: {}", userId);
        return mapToFarmerDto(saved);
    }

    // --- Dealer Profile Methods ---

    public DealerProfileDto getDealerProfile(Long userId) {
        DealerProfile profile = dealerRepo.findByUserId(userId)
                .orElseGet(() -> dealerRepo.save(DealerProfile.builder()
                        .userId(userId)
                        .businessName("Dealer Business #" + userId)
                        .ownerName("Dealer #" + userId)
                        .accountStatus("ACTIVE")
                        .verificationStatus(VerificationStatus.UNVERIFIED)
                        .build()));

        return mapToDealerDto(profile);
    }

    @Transactional
    public DealerProfileDto updateDealerProfile(Long userId, DealerProfileUpdateRequest req) {
        DealerProfile profile = dealerRepo.findByUserId(userId)
                .orElseGet(() -> DealerProfile.builder().userId(userId).accountStatus("ACTIVE").verificationStatus(VerificationStatus.UNVERIFIED).build());

        if (req.getBusinessName() != null) profile.setBusinessName(req.getBusinessName());
        if (req.getOwnerName() != null) profile.setOwnerName(req.getOwnerName());
        if (req.getEmail() != null) profile.setEmail(req.getEmail());
        if (req.getPhone() != null) profile.setPhone(req.getPhone());
        if (req.getAddress() != null) profile.setAddress(req.getAddress());
        if (req.getState() != null) profile.setState(req.getState());
        if (req.getDistrict() != null) profile.setDistrict(req.getDistrict());
        if (req.getLatitude() != null) profile.setLatitude(req.getLatitude());
        if (req.getLongitude() != null) profile.setLongitude(req.getLongitude());
        if (req.getProfileImageUrl() != null) profile.setProfileImageUrl(req.getProfileImageUrl());
        if (req.getBankAccountName() != null) profile.setBankAccountName(req.getBankAccountName());
        if (req.getBankAccountNumber() != null) profile.setBankAccountNumber(req.getBankAccountNumber());
        if (req.getIfscCode() != null) profile.setIfscCode(req.getIfscCode());

        DealerProfile saved = dealerRepo.save(profile);
        log.info("Updated Dealer profile for userId: {}", userId);
        return mapToDealerDto(saved);
    }

    // --- Delivery Partner Profile Methods ---

    public DeliveryPartnerProfileDto getDeliveryPartnerProfile(Long userId) {
        DeliveryPartnerProfile profile = deliveryRepo.findByUserId(userId)
                .orElseGet(() -> deliveryRepo.save(DeliveryPartnerProfile.builder()
                        .userId(userId)
                        .fullName("Delivery Partner #" + userId)
                        .availabilityStatus("AVAILABLE")
                        .accountStatus("ACTIVE")
                        .verificationStatus(VerificationStatus.UNVERIFIED)
                        .build()));

        return mapToDeliveryPartnerDto(profile);
    }

    @Transactional
    public DeliveryPartnerProfileDto updateDeliveryPartnerProfile(Long userId, DeliveryPartnerProfileUpdateRequest req) {
        DeliveryPartnerProfile profile = deliveryRepo.findByUserId(userId)
                .orElseGet(() -> DeliveryPartnerProfile.builder().userId(userId).accountStatus("ACTIVE").verificationStatus(VerificationStatus.UNVERIFIED).build());

        if (req.getFullName() != null) profile.setFullName(req.getFullName());
        if (req.getEmail() != null) profile.setEmail(req.getEmail());
        if (req.getPhone() != null) profile.setPhone(req.getPhone());
        if (req.getAddress() != null) profile.setAddress(req.getAddress());
        if (req.getState() != null) profile.setState(req.getState());
        if (req.getDistrict() != null) profile.setDistrict(req.getDistrict());
        if (req.getLatitude() != null) profile.setLatitude(req.getLatitude());
        if (req.getLongitude() != null) profile.setLongitude(req.getLongitude());
        if (req.getProfileImageUrl() != null) profile.setProfileImageUrl(req.getProfileImageUrl());
        if (req.getVehicleType() != null) profile.setVehicleType(req.getVehicleType());
        if (req.getVehicleNumber() != null) profile.setVehicleNumber(req.getVehicleNumber());
        if (req.getDrivingLicense() != null) profile.setDrivingLicense(req.getDrivingLicense());
        if (req.getAvailabilityStatus() != null) profile.setAvailabilityStatus(req.getAvailabilityStatus());
        if (req.getBankAccountName() != null) profile.setBankAccountName(req.getBankAccountName());
        if (req.getBankAccountNumber() != null) profile.setBankAccountNumber(req.getBankAccountNumber());
        if (req.getIfscCode() != null) profile.setIfscCode(req.getIfscCode());

        DeliveryPartnerProfile saved = deliveryRepo.save(profile);
        log.info("Updated Delivery Partner profile for userId: {}", userId);
        return mapToDeliveryPartnerDto(saved);
    }

    // --- Unified Profile Methods for Any Role ---

    public Object getUnifiedProfile(Long userId) {
        Optional<FarmerProfile> farmer = farmerRepo.findByUserId(userId);
        if (farmer.isPresent()) return mapToFarmerDto(farmer.get());

        Optional<DealerProfile> dealer = dealerRepo.findByUserId(userId);
        if (dealer.isPresent()) return mapToDealerDto(dealer.get());

        Optional<DeliveryPartnerProfile> dp = deliveryRepo.findByUserId(userId);
        if (dp.isPresent()) return mapToDeliveryPartnerDto(dp.get());

        // Default fallback: check if farmer or dealer
        return getFarmerProfile(userId);
    }

    @Transactional
    public Object updateUnifiedProfile(Long userId, UserProfileUpdateRequest req) {
        String role = req.getRole();

        if (role == null) {
            // Auto detect from existing table
            if (farmerRepo.findByUserId(userId).isPresent()) role = "FARMER";
            else if (dealerRepo.findByUserId(userId).isPresent()) role = "DEALER";
            else if (deliveryRepo.findByUserId(userId).isPresent()) role = "DELIVERY_PARTNER";
            else role = "FARMER";
        }

        if ("DEALER".equalsIgnoreCase(role) || "ROLE_DEALER".equalsIgnoreCase(role)) {
            DealerProfileUpdateRequest dReq = DealerProfileUpdateRequest.builder()
                    .businessName(req.getBusinessName() != null ? req.getBusinessName() : req.getFullName())
                    .ownerName(req.getOwnerName() != null ? req.getOwnerName() : req.getFullName())
                    .email(req.getEmail())
                    .phone(req.getPhone())
                    .address(req.getAddress())
                    .state(req.getState())
                    .district(req.getDistrict())
                    .latitude(req.getLatitude())
                    .longitude(req.getLongitude())
                    .profileImageUrl(req.getProfileImageUrl())
                    .bankAccountName(req.getBankAccountName())
                    .bankAccountNumber(req.getBankAccountNumber())
                    .ifscCode(req.getIfscCode())
                    .build();
            return updateDealerProfile(userId, dReq);
        } else if ("DELIVERY_PARTNER".equalsIgnoreCase(role) || "ROLE_DELIVERY_PARTNER".equalsIgnoreCase(role)) {
            DeliveryPartnerProfileUpdateRequest dpReq = DeliveryPartnerProfileUpdateRequest.builder()
                    .fullName(req.getFullName())
                    .email(req.getEmail())
                    .phone(req.getPhone())
                    .address(req.getAddress())
                    .state(req.getState())
                    .district(req.getDistrict())
                    .latitude(req.getLatitude())
                    .longitude(req.getLongitude())
                    .profileImageUrl(req.getProfileImageUrl())
                    .vehicleType(req.getVehicleType())
                    .vehicleNumber(req.getVehicleNumber())
                    .drivingLicense(req.getDrivingLicense())
                    .availabilityStatus(req.getAvailabilityStatus())
                    .bankAccountName(req.getBankAccountName())
                    .bankAccountNumber(req.getBankAccountNumber())
                    .ifscCode(req.getIfscCode())
                    .build();
            return updateDeliveryPartnerProfile(userId, dpReq);
        } else {
            FarmerProfileUpdateRequest fReq = FarmerProfileUpdateRequest.builder()
                    .fullName(req.getFullName())
                    .email(req.getEmail())
                    .phone(req.getPhone())
                    .address(req.getAddress())
                    .state(req.getState())
                    .district(req.getDistrict())
                    .village(req.getVillage())
                    .latitude(req.getLatitude())
                    .longitude(req.getLongitude())
                    .profileImageUrl(req.getProfileImageUrl())
                    .bankAccountName(req.getBankAccountName())
                    .bankAccountNumber(req.getBankAccountNumber())
                    .ifscCode(req.getIfscCode())
                    .build();
            return updateFarmerProfile(userId, fReq);
        }
    }

    @Transactional
    public void createInitialProfile(Long userId, String email, String fullName, String phone, String role) {
        if ("ROLE_DEALER".equalsIgnoreCase(role) || "DEALER".equalsIgnoreCase(role)) {
            DealerProfile d = dealerRepo.findByUserId(userId).orElseGet(() -> DealerProfile.builder().userId(userId).build());
            d.setEmail(email);
            d.setOwnerName(fullName);
            d.setBusinessName(fullName + " Trading Co.");
            d.setPhone(phone);
            d.setAccountStatus("ACTIVE");
            d.setVerificationStatus(VerificationStatus.UNVERIFIED);
            dealerRepo.save(d);
        } else if ("ROLE_DELIVERY_PARTNER".equalsIgnoreCase(role) || "DELIVERY_PARTNER".equalsIgnoreCase(role)) {
            DeliveryPartnerProfile dp = deliveryRepo.findByUserId(userId).orElseGet(() -> DeliveryPartnerProfile.builder().userId(userId).build());
            dp.setEmail(email);
            dp.setFullName(fullName);
            dp.setPhone(phone);
            dp.setAvailabilityStatus("AVAILABLE");
            dp.setAccountStatus("ACTIVE");
            dp.setVerificationStatus(VerificationStatus.UNVERIFIED);
            deliveryRepo.save(dp);
        } else {
            FarmerProfile f = farmerRepo.findByUserId(userId).orElseGet(() -> FarmerProfile.builder().userId(userId).build());
            f.setEmail(email);
            f.setFullName(fullName);
            f.setPhone(phone);
            f.setAccountStatus("ACTIVE");
            f.setVerificationStatus(VerificationStatus.UNVERIFIED);
            farmerRepo.save(f);
        }
    }

    @Transactional
    public void updateVerificationStatus(Long userId, String role, VerificationStatus status) {
        if ("FARMER".equalsIgnoreCase(role) || "ROLE_FARMER".equalsIgnoreCase(role)) {
            farmerRepo.findByUserId(userId).ifPresent(f -> { f.setVerificationStatus(status); farmerRepo.save(f); });
        } else if ("DEALER".equalsIgnoreCase(role) || "ROLE_DEALER".equalsIgnoreCase(role)) {
            dealerRepo.findByUserId(userId).ifPresent(d -> { d.setVerificationStatus(status); dealerRepo.save(d); });
        } else if ("DELIVERY_PARTNER".equalsIgnoreCase(role) || "ROLE_DELIVERY_PARTNER".equalsIgnoreCase(role)) {
            deliveryRepo.findByUserId(userId).ifPresent(dp -> { dp.setVerificationStatus(status); deliveryRepo.save(dp); });
        }
    }

    // --- Get All Profiles Methods ---

    public List<FarmerProfileDto> getAllFarmers() {
        return farmerRepo.findAll().stream()
                .map(this::mapToFarmerDto)
                .collect(Collectors.toList());
    }

    public List<DealerProfileDto> getAllDealers() {
        return dealerRepo.findAll().stream()
                .map(this::mapToDealerDto)
                .collect(Collectors.toList());
    }

    public List<DeliveryPartnerProfileDto> getAllDeliveryPartners() {
        return deliveryRepo.findAll().stream()
                .map(this::mapToDeliveryPartnerDto)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getAllUsers() {
        List<FarmerProfileDto> farmers = getAllFarmers();
        List<DealerProfileDto> dealers = getAllDealers();
        List<DeliveryPartnerProfileDto> deliveryPartners = getAllDeliveryPartners();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalUsers", farmers.size() + dealers.size() + deliveryPartners.size());
        response.put("totalFarmers", farmers.size());
        response.put("totalDealers", dealers.size());
        response.put("totalDeliveryPartners", deliveryPartners.size());
        response.put("farmers", farmers);
        response.put("dealers", dealers);
        response.put("deliveryPartners", deliveryPartners);
        return response;
    }

    public List<Object> getAllUsersList() {
        List<Object> all = new java.util.ArrayList<>();
        all.addAll(getAllFarmers());
        all.addAll(getAllDealers());
        all.addAll(getAllDeliveryPartners());
        return all;
    }

    private FarmerProfileDto mapToFarmerDto(FarmerProfile profile) {
        return FarmerProfileDto.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .fullName(profile.getFullName() != null ? profile.getFullName() : "Farmer #" + profile.getUserId())
                .email(profile.getEmail())
                .phone(profile.getPhone())
                .address(profile.getAddress())
                .state(profile.getState())
                .district(profile.getDistrict())
                .village(profile.getVillage())
                .latitude(profile.getLatitude())
                .longitude(profile.getLongitude())
                .profileImageUrl(profile.getProfileImageUrl())
                .maskedBankAccount(MaskingUtils.maskBankAccount(profile.getBankAccountNumber()))
                .ifscCode(profile.getIfscCode())
                .verificationStatus(profile.getVerificationStatus() != null ? profile.getVerificationStatus() : VerificationStatus.UNVERIFIED)
                .accountStatus(profile.getAccountStatus() != null ? profile.getAccountStatus() : "ACTIVE")
                .build();
    }

    private DealerProfileDto mapToDealerDto(DealerProfile profile) {
        return DealerProfileDto.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .businessName(profile.getBusinessName() != null ? profile.getBusinessName() : "Dealer #" + profile.getUserId())
                .ownerName(profile.getOwnerName() != null ? profile.getOwnerName() : "Dealer Owner")
                .email(profile.getEmail())
                .phone(profile.getPhone())
                .address(profile.getAddress())
                .state(profile.getState())
                .district(profile.getDistrict())
                .latitude(profile.getLatitude())
                .longitude(profile.getLongitude())
                .profileImageUrl(profile.getProfileImageUrl())
                .maskedBankAccount(MaskingUtils.maskBankAccount(profile.getBankAccountNumber()))
                .ifscCode(profile.getIfscCode())
                .verificationStatus(profile.getVerificationStatus() != null ? profile.getVerificationStatus() : VerificationStatus.UNVERIFIED)
                .accountStatus(profile.getAccountStatus() != null ? profile.getAccountStatus() : "ACTIVE")
                .build();
    }

    private DeliveryPartnerProfileDto mapToDeliveryPartnerDto(DeliveryPartnerProfile profile) {
        return DeliveryPartnerProfileDto.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .fullName(profile.getFullName() != null ? profile.getFullName() : "Delivery Partner #" + profile.getUserId())
                .email(profile.getEmail())
                .phone(profile.getPhone())
                .address(profile.getAddress())
                .state(profile.getState())
                .district(profile.getDistrict())
                .latitude(profile.getLatitude())
                .longitude(profile.getLongitude())
                .profileImageUrl(profile.getProfileImageUrl())
                .vehicleType(profile.getVehicleType())
                .vehicleNumber(profile.getVehicleNumber())
                .drivingLicense(profile.getDrivingLicense())
                .availabilityStatus(profile.getAvailabilityStatus() != null ? profile.getAvailabilityStatus() : "AVAILABLE")
                .bankAccountName(profile.getBankAccountName())
                .maskedBankAccount(MaskingUtils.maskBankAccount(profile.getBankAccountNumber()))
                .ifscCode(profile.getIfscCode())
                .verificationStatus(profile.getVerificationStatus() != null ? profile.getVerificationStatus() : VerificationStatus.UNVERIFIED)
                .accountStatus(profile.getAccountStatus() != null ? profile.getAccountStatus() : "ACTIVE")
                .build();
    }
}