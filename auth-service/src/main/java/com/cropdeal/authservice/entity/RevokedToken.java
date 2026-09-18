package com.cropdeal.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "revoked_tokens", indexes = {
    @Index(name = "idx_revoked_token_hash", columnList = "tokenHash", unique = true),
    @Index(name = "idx_revoked_user_id", columnList = "userId"),
    @Index(name = "idx_revoked_expires_at", columnList = "expiresAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevokedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime revokedAt;

    @PrePersist
    public void prePersist() {
        if (this.revokedAt == null) {
            this.revokedAt = LocalDateTime.now();
        }
    }
}
