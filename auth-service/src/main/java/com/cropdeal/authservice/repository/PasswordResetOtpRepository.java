package com.cropdeal.authservice.repository;

import com.cropdeal.authservice.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp> findTopByEmailAndUsedFalseAndVerifiedFalseOrderByCreatedAtDesc(String email);

    List<PasswordResetOtp> findByEmailAndUsedFalse(String email);

    List<PasswordResetOtp> findByUserIdAndUsedFalse(Long userId);

    long countByEmailAndCreatedAtAfter(String email, LocalDateTime since);

    @Modifying
    @Query("DELETE FROM PasswordResetOtp o WHERE o.expiresAt < :now")
    void deleteByExpiresAtBefore(@Param("now") LocalDateTime now);
}
