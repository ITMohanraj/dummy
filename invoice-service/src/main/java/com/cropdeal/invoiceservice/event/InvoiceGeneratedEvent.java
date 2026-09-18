package com.cropdeal.invoiceservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceGeneratedEvent implements Serializable {
    private String eventId;
    private String eventType;
    private Long invoiceId;
    private String invoiceNumber;
    private Long orderId;
    private Long farmerId;
    private Long dealerId;
    private BigDecimal totalAmount;
    private LocalDateTime timestamp;
}