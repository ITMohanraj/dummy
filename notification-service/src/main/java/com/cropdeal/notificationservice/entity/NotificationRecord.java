package com.cropdeal.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long recipientUserId;

    private String role; // FARMER, DEALER, DELIVERY_PARTNER, ADMIN
    private String title;

    @Column(length = 1000, nullable = false)
    private String message;

    private String type; // PRICE_ALERT, ORDER, DELIVERY, PAYMENT, BID
    private boolean isRead;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }
}
