package com.cropdeal.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dealer_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    private String businessName;
    private String ownerName;
    private String email;
    private String phone;
    private String address;
    private String state;
    private String district;
    private Double latitude;
    private Double longitude;
    private String profileImageUrl;

    // Bank Details
    private String bankAccountName;
    private String bankAccountNumber;
    private String ifscCode;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;

    private String accountStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.verificationStatus == null) {
            this.verificationStatus = VerificationStatus.UNVERIFIED;
        }
        if (this.accountStatus == null) {
            this.accountStatus = "ACTIVE";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
