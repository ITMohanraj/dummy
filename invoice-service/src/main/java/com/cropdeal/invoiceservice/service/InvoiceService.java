package com.cropdeal.invoiceservice.service;

import com.cropdeal.invoiceservice.dto.InvoiceResponse;
import com.cropdeal.invoiceservice.entity.FarmerReceipt;
import com.cropdeal.invoiceservice.entity.Invoice;
import com.cropdeal.invoiceservice.entity.InvoiceStatus;
import com.cropdeal.invoiceservice.event.InvoiceGeneratedEvent;
import com.cropdeal.invoiceservice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepo;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public Invoice generateInvoiceFromOrder(Map<String, Object> orderData) {
        Long orderId = Long.valueOf(orderData.get("orderId").toString());

        // Idempotency check: if invoice for this order already exists, return existing
        Optional<Invoice> existing = invoiceRepo.findByOrderId(orderId);
        if (existing.isPresent()) {
            log.info("Invoice for Order #{} already exists (Invoice: {}). Skipping duplicate generation.", orderId, existing.get().getInvoiceNumber());
            return existing.get();
        }

        Long dealerId = orderData.get("dealerId") != null ? Long.valueOf(orderData.get("dealerId").toString()) : 1L;
        Long farmerId = orderData.get("farmerId") != null ? Long.valueOf(orderData.get("farmerId").toString()) : 1L;
        Long cropId = orderData.get("cropId") != null ? Long.valueOf(orderData.get("cropId").toString()) : 1L;
        String cropName = orderData.get("cropName") != null ? orderData.get("cropName").toString() : "Crop Produce";
        Double quantityKg = orderData.get("quantityKg") != null ? Double.valueOf(orderData.get("quantityKg").toString()) : 100.0;
        
        BigDecimal pricePerKg = orderData.get("pricePerKg") != null ? 
                new BigDecimal(orderData.get("pricePerKg").toString()) : new BigDecimal("25.00");
        BigDecimal totalAmount = orderData.get("totalAmount") != null ?
                new BigDecimal(orderData.get("totalAmount").toString()) : pricePerKg.multiply(BigDecimal.valueOf(quantityKg));

        String paymentRef = orderData.get("paymentId") != null ? orderData.get("paymentId").toString() : "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String invoiceNum = "INV-" + orderId + "-" + (System.currentTimeMillis() % 100000);

        Invoice invoice = Invoice.builder()
                .orderId(orderId)
                .invoiceNumber(invoiceNum)
                .dealerId(dealerId)
                .dealerName("Dealer #" + dealerId)
                .farmerId(farmerId)
                .farmerName("Farmer #" + farmerId)
                .cropId(cropId)
                .cropName(cropName)
                .quantityKg(quantityKg)
                .pricePerKg(pricePerKg)
                .cropSubtotal(totalAmount)
                .deliveryCharge(BigDecimal.ZERO)
                .platformFee(BigDecimal.ZERO)
                .totalAmount(totalAmount)
                .paymentReference(paymentRef)
                .paymentStatus("PAID")
                .invoiceStatus(InvoiceStatus.GENERATED)
                .build();

        Invoice saved = invoiceRepo.save(invoice);
        log.info("Successfully generated Invoice #{} for Order #{}", saved.getInvoiceNumber(), orderId);

        // Publish InvoiceGeneratedEvent
        InvoiceGeneratedEvent event = InvoiceGeneratedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("INVOICE_GENERATED")
                .invoiceId(saved.getId())
                .invoiceNumber(saved.getInvoiceNumber())
                .orderId(saved.getOrderId())
                .farmerId(saved.getFarmerId())
                .dealerId(saved.getDealerId())
                .totalAmount(saved.getTotalAmount())
                .timestamp(LocalDateTime.now())
                .build();

        try {
            rabbitTemplate.convertAndSend("invoice.exchange", "invoice.generated", event);
            log.info("Published invoice.generated event to RabbitMQ for Order #{}", orderId);
        } catch (Exception e) {
            log.warn("Failed to publish invoice.generated event: {}", e.getMessage());
        }

        return saved;
    }

    public Invoice getByOrderId(Long orderId) {
        return invoiceRepo.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found for order #" + orderId));
    }

    public Invoice getById(Long id) {
        return invoiceRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + id));
    }

    public List<InvoiceResponse> getByDealerId(Long dealerId) {
        return invoiceRepo.findByDealerId(dealerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<InvoiceResponse> getByFarmerId(Long farmerId) {
        return invoiceRepo.findByFarmerId(farmerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public FarmerReceipt getFarmerReceiptByOrderId(Long orderId) {
        Invoice invoice = getByOrderId(orderId);
        return FarmerReceipt.builder()
                .id(invoice.getId())
                .receiptNumber("RCP-" + invoice.getOrderId() + "-" + (invoice.getId() % 10000))
                .orderId(invoice.getOrderId())
                .farmerId(invoice.getFarmerId())
                .dealerId(invoice.getDealerId())
                .cropName(invoice.getCropName())
                .quantityKg(invoice.getQuantityKg())
                .pricePerKg(invoice.getPricePerKg())
                .totalAmount(invoice.getTotalAmount())
                .paymentReference(invoice.getPaymentReference())
                .issuedAt(invoice.getIssuedAt())
                .build();
    }

    public InvoiceResponse mapToResponse(Invoice inv) {
        return InvoiceResponse.builder()
                .id(inv.getId())
                .orderId(inv.getOrderId())
                .invoiceNumber(inv.getInvoiceNumber())
                .dealerId(inv.getDealerId())
                .dealerName(inv.getDealerName())
                .farmerId(inv.getFarmerId())
                .farmerName(inv.getFarmerName())
                .cropId(inv.getCropId())
                .cropName(inv.getCropName())
                .quantityKg(inv.getQuantityKg())
                .pricePerKg(inv.getPricePerKg())
                .cropSubtotal(inv.getCropSubtotal())
                .deliveryCharge(inv.getDeliveryCharge())
                .platformFee(inv.getPlatformFee())
                .totalAmount(inv.getTotalAmount())
                .paymentReference(inv.getPaymentReference())
                .paymentStatus(inv.getPaymentStatus())
                .pickupLocation(inv.getPickupLocation())
                .deliveryLocation(inv.getDeliveryLocation())
                .invoiceStatus(inv.getInvoiceStatus())
                .issuedAt(inv.getIssuedAt())
                .build();
    }
}