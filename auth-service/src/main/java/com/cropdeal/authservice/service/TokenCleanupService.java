package com.cropdeal.authservice.service;

import com.cropdeal.authservice.repository.PasswordResetOtpRepository;
import com.cropdeal.authservice.repository.PasswordResetTokenRepository;
import com.cropdeal.authservice.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final RevokedTokenRepository revokedTokenRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Scheduled(cron = "${cropdeal.cleanup.cron:0 0 * * * *}") // Run every hour
    @Transactional
    public void cleanupExpiredTokensAndOtps() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Running periodic cleanup for expired tokens and OTPs at {}", now);

        try {
            revokedTokenRepository.deleteByExpiresAtBefore(now);
            passwordResetOtpRepository.deleteByExpiresAtBefore(now);
            passwordResetTokenRepository.deleteByExpiresAtBefore(now);
            log.info("Cleanup completed successfully");
        } catch (Exception e) {
            log.error("Error during scheduled token/OTP cleanup: {}", e.getMessage());
        }
    }
}
