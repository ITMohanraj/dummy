package com.cropdeal.reviewservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "farmer_reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"orderId", "dealerId"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long dealerId;

    @Column(nullable = false)
    private Long farmerId;

    @Column(nullable = false)
    private Integer rating; // 1 to 5

    @Column(length = 1000)
    private String comment;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
