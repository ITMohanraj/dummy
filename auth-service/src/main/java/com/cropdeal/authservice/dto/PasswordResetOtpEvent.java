package com.cropdeal.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetOtpEvent implements Serializable {
    private String eventType;
    private Long userId;
    private String email;
    private String otp;
    private int expiresInMinutes;
    private LocalDateTime timestamp;
}
