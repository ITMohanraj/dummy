package com.cropdeal.auditservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long actorUserId;
    private String actorRole; // ADMIN, FARMER, DEALER, DELIVERY_PARTNER, SYSTEM

    @Column(nullable = false)
    private String action; // USER_REGISTERED, CROP_POSTED, ORDER_CREATED, PAYMENT_SUCCESS

    private String entityType; // User, Crop, Order, Payment, Delivery
    private String entityId;

    @Column(length = 1500)
    private String description;

    private String ipAddress;
    private String correlationId;

    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }
}
