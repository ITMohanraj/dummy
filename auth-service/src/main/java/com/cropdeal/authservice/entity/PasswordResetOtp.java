package com.cropdeal.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_otps", indexes = {
    @Index(name = "idx_otp_email", columnList = "email"),
    @Index(name = "idx_otp_user_id", columnList = "userId"),
    @Index(name = "idx_otp_expires_at", columnList = "expiresAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String otpHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Integer attempts;

    @Column(nullable = false)
    private Boolean verified;

    @Column(nullable = false)
    private Boolean used;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.attempts == null) {
            this.attempts = 0;
        }
        if (this.verified == null) {
            this.verified = false;
        }
        if (this.used == null) {
            this.used = false;
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
