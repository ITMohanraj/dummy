package com.cropdeal.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reset Password Request")
public class ResetPasswordRequest {

    @NotBlank(message = "Reset token is required")
    @Schema(description = "Password reset token obtained from verify-otp")
    private String resetToken;

    @NotBlank(message = "New password is required")
    @Schema(description = "New password (min 8 chars, 1 uppercase, 1 lowercase, 1 number, 1 special char)", example = "NewPassword@123")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    @Schema(description = "Confirm new password", example = "NewPassword@123")
    private String confirmPassword;
}
