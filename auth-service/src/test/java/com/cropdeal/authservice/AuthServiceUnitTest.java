package com.cropdeal.authservice;

import com.cropdeal.authservice.dto.*;
import com.cropdeal.authservice.entity.*;
import com.cropdeal.authservice.exception.*;
import com.cropdeal.authservice.repository.*;
import com.cropdeal.authservice.security.JwtUtils;
import com.cropdeal.authservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {

    @Mock
    private UserAuthRepository userAuthRepository;

    @Mock
    private PasswordResetOtpRepository passwordResetOtpRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private RevokedTokenRepository revokedTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AuthService authService;

    private UserAuth sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = UserAuth.builder()
                .id(1L)
                .email("farmer@gmail.com")
                .passwordHash("$2a$10$encodedOldPassword")
                .role(Role.ROLE_FARMER)
                .fullName("Ramesh Farmer")
                .status(AccountStatus.ACTIVE)
                .build();
    }

    // 1. Forgot password with existing email
    @Test
    void testForgotPassword_ExistingEmail_GeneratesOtp() {
        when(userAuthRepository.findByEmail("farmer@gmail.com")).thenReturn(Optional.of(sampleUser));
        when(passwordResetOtpRepository.countByEmailAndCreatedAtAfter(anyString(), any())).thenReturn(0L);
        when(passwordResetOtpRepository.findByUserIdAndUsedFalse(1L)).thenReturn(Collections.emptyList());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedOtp");

        ForgotPasswordRequest request = ForgotPasswordRequest.builder().email("farmer@gmail.com").build();
        MessageResponse response = authService.forgotPassword(request);

        assertNotNull(response);
        assertEquals("If an account exists for this email, a password reset OTP has been sent.", response.getMessage());
        verify(passwordResetOtpRepository, times(1)).save(any(PasswordResetOtp.class));
    }

    // 2. Forgot password with non-existing email (no account enumeration)
    @Test
    void testForgotPassword_NonExistingEmail_ReturnsGenericMessage() {
        when(userAuthRepository.findByEmail("nonexistent@gmail.com")).thenReturn(Optional.empty());

        ForgotPasswordRequest request = ForgotPasswordRequest.builder().email("nonexistent@gmail.com").build();
        MessageResponse response = authService.forgotPassword(request);

        assertNotNull(response);
        assertEquals("If an account exists for this email, a password reset OTP has been sent.", response.getMessage());
        verify(passwordResetOtpRepository, never()).save(any());
    }

    // 3. OTP rate limit
    @Test
    void testForgotPassword_RateLimit_ThrowsException() {
        when(userAuthRepository.findByEmail("farmer@gmail.com")).thenReturn(Optional.of(sampleUser));
        when(passwordResetOtpRepository.countByEmailAndCreatedAtAfter(anyString(), any())).thenReturn(1L);

        ForgotPasswordRequest request = ForgotPasswordRequest.builder().email("farmer@gmail.com").build();
        assertThrows(OtpRateLimitException.class, () -> authService.forgotPassword(request));
    }

    // 4. Verify OTP - Correct OTP
    @Test
    void testVerifyOtp_CorrectOtp_ReturnsResetToken() {
        PasswordResetOtp otpRecord = PasswordResetOtp.builder()
                .id(10L)
                .userId(1L)
                .email("farmer@gmail.com")
                .otpHash("$2a$10$hashedOtp")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .attempts(0)
                .verified(false)
                .used(false)
                .build();

        when(passwordResetOtpRepository.findTopByEmailAndUsedFalseAndVerifiedFalseOrderByCreatedAtDesc("farmer@gmail.com"))
                .thenReturn(Optional.of(otpRecord));
        when(passwordEncoder.matches("123456", "$2a$10$hashedOtp")).thenReturn(true);

        VerifyOtpRequest request = VerifyOtpRequest.builder().email("farmer@gmail.com").otp("123456").build();
        VerifyOtpResponse response = authService.verifyForgotPasswordOtp(request);

        assertNotNull(response);
        assertEquals("OTP verified successfully", response.getMessage());
        assertNotNull(response.getResetToken());
        assertTrue(otpRecord.getVerified());
        verify(passwordResetTokenRepository, times(1)).save(any(PasswordResetToken.class));
    }

    // 5. Verify OTP - Incorrect OTP
    @Test
    void testVerifyOtp_IncorrectOtp_IncrementsAttempts() {
        PasswordResetOtp otpRecord = PasswordResetOtp.builder()
                .id(10L)
                .userId(1L)
                .email("farmer@gmail.com")
                .otpHash("$2a$10$hashedOtp")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .attempts(0)
                .verified(false)
                .used(false)
                .build();

        when(passwordResetOtpRepository.findTopByEmailAndUsedFalseAndVerifiedFalseOrderByCreatedAtDesc("farmer@gmail.com"))
                .thenReturn(Optional.of(otpRecord));
        when(passwordEncoder.matches("999999", "$2a$10$hashedOtp")).thenReturn(false);

        VerifyOtpRequest request = VerifyOtpRequest.builder().email("farmer@gmail.com").otp("999999").build();
        assertThrows(InvalidOtpException.class, () -> authService.verifyForgotPasswordOtp(request));
        assertEquals(1, otpRecord.getAttempts());
    }

    // 6. Verify OTP - Expired OTP
    @Test
    void testVerifyOtp_ExpiredOtp_ThrowsOtpExpiredException() {
        PasswordResetOtp otpRecord = PasswordResetOtp.builder()
                .id(10L)
                .userId(1L)
                .email("farmer@gmail.com")
                .otpHash("$2a$10$hashedOtp")
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .attempts(0)
                .verified(false)
                .used(false)
                .build();

        when(passwordResetOtpRepository.findTopByEmailAndUsedFalseAndVerifiedFalseOrderByCreatedAtDesc("farmer@gmail.com"))
                .thenReturn(Optional.of(otpRecord));

        VerifyOtpRequest request = VerifyOtpRequest.builder().email("farmer@gmail.com").otp("123456").build();
        assertThrows(OtpExpiredException.class, () -> authService.verifyForgotPasswordOtp(request));
        assertTrue(otpRecord.getUsed());
    }

    // 7. Verify OTP - Max attempts exceeded
    @Test
    void testVerifyOtp_MaxAttempts_ThrowsException() {
        PasswordResetOtp otpRecord = PasswordResetOtp.builder()
                .id(10L)
                .userId(1L)
                .email("farmer@gmail.com")
                .otpHash("$2a$10$hashedOtp")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .attempts(5)
                .verified(false)
                .used(false)
                .build();

        when(passwordResetOtpRepository.findTopByEmailAndUsedFalseAndVerifiedFalseOrderByCreatedAtDesc("farmer@gmail.com"))
                .thenReturn(Optional.of(otpRecord));

        VerifyOtpRequest request = VerifyOtpRequest.builder().email("farmer@gmail.com").otp("123456").build();
        assertThrows(TooManyOtpAttemptsException.class, () -> authService.verifyForgotPasswordOtp(request));
        assertTrue(otpRecord.getUsed());
    }

    // 8. Reset Password with valid token
    @Test
    void testResetPassword_ValidToken_Success() {
        PasswordResetToken token = PasswordResetToken.builder()
                .id(1L)
                .userId(1L)
                .tokenHash(JwtUtils.hashToken("valid-token"))
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        when(passwordResetTokenRepository.findByTokenHashAndUsedFalse(anyString())).thenReturn(Optional.of(token));
        when(userAuthRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("NewPassword@123", "$2a$10$encodedOldPassword")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword@123")).thenReturn("$2a$10$encodedNewPassword");
        when(passwordResetOtpRepository.findByUserIdAndUsedFalse(1L)).thenReturn(Collections.emptyList());

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .resetToken("valid-token")
                .newPassword("NewPassword@123")
                .confirmPassword("NewPassword@123")
                .build();

        MessageResponse response = authService.resetPassword(request);
        assertEquals("Password reset successfully", response.getMessage());
        assertTrue(token.getUsed());
        assertNotNull(sampleUser.getPasswordChangedAt());
    }

    // 9. Reset Password - Password Mismatch
    @Test
    void testResetPassword_PasswordMismatch_ThrowsException() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .resetToken("valid-token")
                .newPassword("NewPassword@123")
                .confirmPassword("DifferentPassword@123")
                .build();

        assertThrows(PasswordMismatchException.class, () -> authService.resetPassword(request));
    }

    // 10. Reset Password - Password Reuse
    @Test
    void testResetPassword_PasswordReuse_ThrowsException() {
        PasswordResetToken token = PasswordResetToken.builder()
                .id(1L)
                .userId(1L)
                .tokenHash(JwtUtils.hashToken("valid-token"))
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        when(passwordResetTokenRepository.findByTokenHashAndUsedFalse(anyString())).thenReturn(Optional.of(token));
        when(userAuthRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("NewPassword@123", "$2a$10$encodedOldPassword")).thenReturn(true);

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .resetToken("valid-token")
                .newPassword("NewPassword@123")
                .confirmPassword("NewPassword@123")
                .build();

        assertThrows(PasswordReuseException.class, () -> authService.resetPassword(request));
    }

    // 11. Reset Password - Weak Password
    @Test
    void testResetPassword_WeakPassword_ThrowsException() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .resetToken("valid-token")
                .newPassword("weak")
                .confirmPassword("weak")
                .build();

        assertThrows(WeakPasswordException.class, () -> authService.resetPassword(request));
    }

    // 12. Change Password - Correct Current Password
    @Test
    void testChangePassword_CorrectCurrentPassword_Success() {
        when(userAuthRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("OldPassword@123", "$2a$10$encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword@123", "$2a$10$encodedOldPassword")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword@123")).thenReturn("$2a$10$encodedNewPassword");

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("OldPassword@123")
                .newPassword("NewPassword@123")
                .confirmPassword("NewPassword@123")
                .build();

        MessageResponse response = authService.changePassword(1L, request);
        assertEquals("Password changed successfully", response.getMessage());
        assertNotNull(sampleUser.getPasswordChangedAt());
    }

    // 13. Change Password - Incorrect Current Password
    @Test
    void testChangePassword_IncorrectCurrentPassword_ThrowsException() {
        when(userAuthRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("WrongPassword@123", "$2a$10$encodedOldPassword")).thenReturn(false);

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("WrongPassword@123")
                .newPassword("NewPassword@123")
                .confirmPassword("NewPassword@123")
                .build();

        assertThrows(InvalidCurrentPasswordException.class, () -> authService.changePassword(1L, request));
    }

    // 14. Logout & Revoke JWT
    @Test
    void testLogout_ValidBearerToken_SavesToRevokedTokens() {
        String token = "mock-valid-token";
        String authHeader = "Bearer " + token;

        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(revokedTokenRepository.existsByTokenHash(anyString())).thenReturn(false);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(1L);
        when(jwtUtils.getExpirationFromToken(token)).thenReturn(new Date(System.currentTimeMillis() + 3600000));

        MessageResponse response = authService.logout(authHeader);
        assertEquals("Logged out successfully", response.getMessage());
        verify(revokedTokenRepository, times(1)).save(any(RevokedToken.class));
    }
}
