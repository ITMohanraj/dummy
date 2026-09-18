package com.cropdeal.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OTP Verification Response")
public class VerifyOtpResponse {

    @Schema(description = "Status message", example = "OTP verified successfully")
    private String message;

    @Schema(description = "Short-lived password reset token to be used on reset-password endpoint")
    private String resetToken;
}
