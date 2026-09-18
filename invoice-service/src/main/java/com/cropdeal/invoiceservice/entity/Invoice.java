package com.cropdeal.invoiceservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices",
       indexes = {
           @Index(name = "idx_inv_order", columnList = "orderId", unique = true),
           @Index(name = "idx_inv_number", columnList = "invoiceNumber", unique = true),
           @Index(name = "idx_inv_dealer", columnList = "dealerId"),
           @Index(name = "idx_inv_farmer", columnList = "farmerId")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @Column(nullable = false)
    private Long dealerId;

    private String dealerName;
    private String dealerEmail;
    private String dealerPhone;

    @Column(nullable = false)
    private Long farmerId;

    private String farmerName;
    private String farmerEmail;
    private String farmerPhone;

    private Long cropId;
    private String cropName;
    private String cropVariety;

    private Double quantityKg;
    private BigDecimal pricePerKg;
    private BigDecimal cropSubtotal;

    @Builder.Default
    private BigDecimal deliveryCharge = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal platformFee = BigDecimal.ZERO;

    private BigDecimal totalAmount;

    private String paymentMethod;
    private String paymentReference;
    private String paymentStatus;

    private String pickupLocation;
    private String deliveryLocation;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private InvoiceStatus invoiceStatus = InvoiceStatus.GENERATED;

    @CreationTimestamp
    private LocalDateTime issuedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}