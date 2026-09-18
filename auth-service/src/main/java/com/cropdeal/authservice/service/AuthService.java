package com.cropdeal.authservice.service;

import com.cropdeal.authservice.dto.*;
import com.cropdeal.authservice.entity.*;
import com.cropdeal.authservice.exception.*;
import com.cropdeal.authservice.repository.PasswordResetOtpRepository;
import com.cropdeal.authservice.repository.PasswordResetTokenRepository;
import com.cropdeal.authservice.repository.RevokedTokenRepository;
import com.cropdeal.authservice.repository.UserAuthRepository;
import com.cropdeal.authservice.security.JwtUtils;
import com.cropdeal.authservice.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserAuthRepository userAuthRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RabbitTemplate rabbitTemplate;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${cropdeal.rabbitmq.exchange.auth:auth.exchange}")
    private String authExchange;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getRole() == Role.ROLE_ADMIN) {
            throw new UnauthorizedOperationException("Admin registration is not permitted via public registration API");
        }

        PasswordValidator.validate(request.getPassword());

        if (userAuthRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        UserAuth user = UserAuth.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .status(AccountStatus.ACTIVE)
                .build();

        UserAuth savedUser = userAuthRepository.save(user);

        // Publish event
        try {
            UserRegisteredEvent event = UserRegisteredEvent.builder()
                    .userId(savedUser.getId())
                    .email(savedUser.getEmail())
                    .fullName(savedUser.getFullName())
                    .role(savedUser.getRole().name())
                    .phone(savedUser.getPhone())
                    .registeredAt(LocalDateTime.now())
                    .build();
            rabbitTemplate.convertAndSend(authExchange, "user.registered", event);
        } catch (Exception e) {
            log.warn("Failed to publish user.registered event: {}", e.getMessage());
        }

        String token = jwtUtils.generateToken(savedUser);
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .fullName(savedUser.getFullName())
                .expiresIn(jwtUtils.getExpirationMs())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        UserAuth user = userAuthRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new UnauthorizedOperationException("Account is " + user.getStatus());
        }

        String token = jwtUtils.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .fullName(user.getFullName())
                .expiresIn(jwtUtils.getExpirationMs())
                .build();
    }

    @Transactional
    public AuthResponse loginWithFacebook(FacebookLoginRequest request) {
        UserAuth user = userAuthRepository.findByFacebookId(request.getFacebookId())
                .or(() -> userAuthRepository.findByEmail(request.getEmail()))
                .orElseGet(() -> {
                    Role role = request.getRole() != null && request.getRole() != Role.ROLE_ADMIN ?
                            request.getRole() : Role.ROLE_DEALER;
                    UserAuth newUser = UserAuth.builder()
                            .email(request.getEmail())
                            .fullName(request.getFullName())
                            .facebookId(request.getFacebookId())
                            .passwordHash(passwordEncoder.encode("OAuth2_FB_" + request.getFacebookId()))
                            .role(role)
                            .status(AccountStatus.ACTIVE)
                            .build();
                    return userAuthRepository.save(newUser);
                });

        String token = jwtUtils.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .fullName(user.getFullName())
                .expiresIn(jwtUtils.getExpirationMs())
                .build();
    }

    @Transactional
    public AuthResponse createAdmin(AdminCreateRequest request) {
        PasswordValidator.validate(request.getPassword());

        if (userAuthRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Admin with email " + request.getEmail() + " already exists");
        }

        UserAuth admin = UserAuth.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_ADMIN)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .status(AccountStatus.ACTIVE)
                .build();

        UserAuth savedAdmin = userAuthRepository.save(admin);
        String token = jwtUtils.generateToken(savedAdmin);
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(savedAdmin.getId())
                .email(savedAdmin.getEmail())
                .role(savedAdmin.getRole())
                .fullName(savedAdmin.getFullName())
                .expiresIn(jwtUtils.getExpirationMs())
                .build();
    }

    @Transactional
    public UserAuth updateUserStatus(Long userId, AccountStatus status) {
        UserAuth user = userAuthRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("User not found: " + userId));
        user.setStatus(status);
        return userAuthRepository.save(user);
    }

    // ==========================================
    // 1. FORGOT PASSWORD (OTP GENERATION)
    // ==========================================
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        Optional<UserAuth> userOpt = userAuthRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            log.info("Forgot password requested for non-existent email");
            return MessageResponse.builder()
                    .message("If an account exists for this email, a password reset OTP has been sent.")
                    .build();
        }

        UserAuth user = userOpt.get();

        // Check cooldown / rate limit (max 1 request per minute)
        long recentRequests = passwordResetOtpRepository.countByEmailAndCreatedAtAfter(
                user.getEmail(), LocalDateTime.now().minusMinutes(1));
        if (recentRequests > 0) {
            throw new OtpRateLimitException("Please wait at least 1 minute before requesting another OTP.");
        }

        // Invalidate previous active OTPs for this user
        List<PasswordResetOtp> activeOtps = passwordResetOtpRepository.findByUserIdAndUsedFalse(user.getId());
        for (PasswordResetOtp activeOtp : activeOtps) {
            activeOtp.setUsed(true);
            passwordResetOtpRepository.save(activeOtp);
        }

        // Generate cryptographically secure 6-digit OTP
        String otp = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));

        // Store hashed OTP
        PasswordResetOtp otpEntity = PasswordResetOtp.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .otpHash(passwordEncoder.encode(otp))
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .attempts(0)
                .verified(false)
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        passwordResetOtpRepository.save(otpEntity);

        // Publish event to RabbitMQ for notification-service (Gmail SMTP)
        try {
            PasswordResetOtpEvent event = PasswordResetOtpEvent.builder()
                    .eventType("PASSWORD_RESET_OTP")
                    .userId(user.getId())
                    .email(user.getEmail())
                    .otp(otp)
                    .expiresInMinutes(5)
                    .timestamp(LocalDateTime.now())
                    .build();
            rabbitTemplate.convertAndSend(authExchange, "auth.password-reset.otp", event);
            log.info("Published password reset OTP event for user ID: {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to publish password reset OTP event to RabbitMQ: {}", e.getMessage());
        }

        return MessageResponse.builder()
                .message("If an account exists for this email, a password reset OTP has been sent.")
                .build();
    }

    // ==========================================
    // 2. VERIFY FORGOT-PASSWORD OTP
    // ==========================================
    @Transactional
    public VerifyOtpResponse verifyForgotPasswordOtp(VerifyOtpRequest request) {
        PasswordResetOtp otpRecord = passwordResetOtpRepository
                .findTopByEmailAndUsedFalseAndVerifiedFalseOrderByCreatedAtDesc(request.getEmail())
                .orElseThrow(() -> new InvalidOtpException("Invalid or expired OTP."));

        // Check if expired
        if (otpRecord.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpRecord.setUsed(true);
            passwordResetOtpRepository.save(otpRecord);
            throw new OtpExpiredException("OTP has expired. Please request a new one.");
        }

        // Check attempts limit
        if (otpRecord.getAttempts() >= 5) {
            otpRecord.setUsed(true);
            passwordResetOtpRepository.save(otpRecord);
            throw new TooManyOtpAttemptsException("Maximum OTP verification attempts exceeded. Please request a new OTP.");
        }

        // Verify OTP hash
        if (!passwordEncoder.matches(request.getOtp(), otpRecord.getOtpHash())) {
            otpRecord.setAttempts(otpRecord.getAttempts() + 1);
            if (otpRecord.getAttempts() >= 5) {
                otpRecord.setUsed(true);
            }
            passwordResetOtpRepository.save(otpRecord);
            int remaining = 5 - otpRecord.getAttempts();
            throw new InvalidOtpException("Invalid OTP. Remaining attempts: " + Math.max(0, remaining));
        }

        // Mark OTP as verified
        otpRecord.setVerified(true);
        passwordResetOtpRepository.save(otpRecord);

        // Generate high-entropy password reset token
        String rawResetToken = UUID.randomUUID().toString().replace("-", "") +
                UUID.randomUUID().toString().replace("-", "");
        String tokenHash = JwtUtils.hashToken(rawResetToken);

        PasswordResetToken resetTokenEntity = PasswordResetToken.builder()
                .userId(otpRecord.getUserId())
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        passwordResetTokenRepository.save(resetTokenEntity);

        return VerifyOtpResponse.builder()
                .message("OTP verified successfully")
                .resetToken(rawResetToken)
                .build();
    }

    // ==========================================
    // 3. RESET PASSWORD (WITH RESET TOKEN)
    // ==========================================
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("New password and confirm password do not match.");
        }

        PasswordValidator.validate(request.getNewPassword());

        String tokenHash = JwtUtils.hashToken(request.getResetToken());
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenHashAndUsedFalse(tokenHash)
                .orElseThrow(() -> new PasswordResetTokenException("Invalid or expired password reset token."));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);
            throw new PasswordResetTokenException("Password reset token has expired.");
        }

        UserAuth user = userAuthRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        // Prevent reusing same password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new PasswordReuseException("New password cannot be the same as the old password.");
        }

        // Update password and record changed timestamp
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userAuthRepository.save(user);

        // Mark reset token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Invalidate all pending OTPs for this user
        List<PasswordResetOtp> activeOtps = passwordResetOtpRepository.findByUserIdAndUsedFalse(user.getId());
        for (PasswordResetOtp otp : activeOtps) {
            otp.setUsed(true);
            passwordResetOtpRepository.save(otp);
        }

        log.info("Password successfully reset for user ID: {}", user.getId());
        return MessageResponse.builder()
                .message("Password reset successfully")
                .build();
    }

    // ==========================================
    // 4. CHANGE PASSWORD FROM PROFILE
    // ==========================================
    @Transactional
    public MessageResponse changePassword(Long userId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("New password and confirm password do not match.");
        }

        PasswordValidator.validate(request.getNewPassword());

        UserAuth user = userAuthRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("User not found: " + userId));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidCurrentPasswordException("Current password is incorrect.");
        }

        // Prevent reusing same password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new PasswordReuseException("New password cannot be the same as the old password.");
        }

        // Update password and record changed timestamp
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userAuthRepository.save(user);

        log.info("Password successfully changed for user ID: {}", userId);
        return MessageResponse.builder()
                .message("Password changed successfully")
                .build();
    }

    // ==========================================
    // 6. LOGOUT (JWT REVOCATION)
    // ==========================================
    @Transactional
    public MessageResponse logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return MessageResponse.builder().message("Logged out successfully").build();
        }

        String token = authHeader.substring(7);

        if (!jwtUtils.validateToken(token)) {
            return MessageResponse.builder().message("Logged out successfully").build();
        }

        String tokenHash = JwtUtils.hashToken(token);

        if (!revokedTokenRepository.existsByTokenHash(tokenHash)) {
            try {
                Long userId = jwtUtils.getUserIdFromToken(token);
                Date expiration = jwtUtils.getExpirationFromToken(token);
                LocalDateTime expiresAt = expiration.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                RevokedToken revokedToken = RevokedToken.builder()
                        .tokenHash(tokenHash)
                        .userId(userId)
                        .expiresAt(expiresAt)
                        .revokedAt(LocalDateTime.now())
                        .build();

                revokedTokenRepository.save(revokedToken);
                log.info("JWT token revoked for user ID: {}", userId);
            } catch (Exception e) {
                log.error("Failed to revoke token: {}", e.getMessage());
            }
        }

        return MessageResponse.builder()
                .message("Logged out successfully")
                .build();
    }
}
