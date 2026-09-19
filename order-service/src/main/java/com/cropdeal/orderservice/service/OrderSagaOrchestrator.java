package com.cropdeal.orderservice.service;

import com.cropdeal.orderservice.dto.OrderResponse;
import com.cropdeal.orderservice.dto.PurchaseRequest;
import com.cropdeal.orderservice.entity.CropOrder;
import com.cropdeal.orderservice.entity.OrderStatus;
import com.cropdeal.orderservice.repository.CropOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSagaOrchestrator {

    private final CropOrderRepository orderRepo;
    private final RabbitTemplate rabbitTemplate;
    private final WebClient.Builder webClientBuilder;

    @Value("${cropdeal.crop-service.url:http://crop-service:8083}")
    private String cropServiceUrl;

    @Value("${cropdeal.payment-service.url:http://payment-service:8086}")
    private String paymentServiceUrl;

    @Value("${cropdeal.wallet-service.url:http://wallet-service:8087}")
    private String walletServiceUrl;

    @Transactional
    public OrderResponse executePurchaseSaga(PurchaseRequest req) {
        log.info("Starting Purchase Saga for Dealer: {}, Crop: {}, Quantity: {} KG, Price/KG: ₹{}",
                req.getDealerId(), req.getCropId(), req.getQuantityKg(), req.getPricePerKg());

        BigDecimal total = req.getPricePerKg().multiply(BigDecimal.valueOf(req.getQuantityKg()));

        // 1. Create Order in PENDING state
        CropOrder order = CropOrder.builder()
                .dealerId(req.getDealerId())
                .farmerId(req.getFarmerId())
                .cropId(req.getCropId())
                .cropName(req.getCropName() != null ? req.getCropName() : "Crop #" + req.getCropId())
                .quantityKg(req.getQuantityKg())
                .pricePerKg(req.getPricePerKg())
                .totalAmount(total)
                .orderType("DIRECT")
                .status(OrderStatus.PENDING)
                .build();
        CropOrder savedOrder = orderRepo.save(order);

        // 2. Step: Reserve Crop Quantity in crop-service with Circuit Breaker
        try {
            reserveCropInventoryWithCircuitBreaker(req.getCropId(), req.getQuantityKg());
            log.info("Crop quantity reserved successfully in crop-service.");
        } catch (Exception e) {
            log.error("Failed to reserve crop quantity: {}", e.getMessage());
            savedOrder.setStatus(OrderStatus.CANCELLED);
            orderRepo.save(savedOrder);
            throw new RuntimeException("Failed to reserve crop inventory: " + e.getMessage());
        }

        // 3. Step: Simulate / Process Payment
        String paymentTxnId = "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 4. Order Confirmed
        savedOrder.setPaymentId(paymentTxnId);
        savedOrder.setStatus(OrderStatus.CONFIRMED);
        CropOrder confirmedOrder = orderRepo.save(savedOrder);

        // 5. Publish ORDER_CONFIRMED & ORDER_PAID event to RabbitMQ
        // Invoice Service, Notification Service, Delivery Service consume this
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("orderId", confirmedOrder.getId());
            eventData.put("dealerId", confirmedOrder.getDealerId());
            eventData.put("farmerId", confirmedOrder.getFarmerId());
            eventData.put("cropId", confirmedOrder.getCropId());
            eventData.put("cropName", confirmedOrder.getCropName());
            eventData.put("quantityKg", confirmedOrder.getQuantityKg());
            eventData.put("pricePerKg", confirmedOrder.getPricePerKg());
            eventData.put("totalAmount", confirmedOrder.getTotalAmount());
            eventData.put("paymentId", confirmedOrder.getPaymentId());
            eventData.put("status", confirmedOrder.getStatus().name());

            rabbitTemplate.convertAndSend("order.exchange", "order.confirmed", eventData);
            rabbitTemplate.convertAndSend("order.exchange", "order.paid", eventData);
            log.info("Published order.confirmed & order.paid events for Order #{}", confirmedOrder.getId());
        } catch (Exception e) {
            log.warn("Failed to publish order event: {}", e.getMessage());
        }

        return mapToResponse(confirmedOrder);
    }

    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        CropOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        order.setStatus(status);
        CropOrder saved = orderRepo.save(order);

        try {
            rabbitTemplate.convertAndSend("order.exchange", "order.updated", Map.of(
                    "orderId", saved.getId(),
                    "status", saved.getStatus().name()
            ));
        } catch (Exception e) {
            log.warn("Failed to publish order.updated event: {}", e.getMessage());
        }

        return mapToResponse(saved);
    }

    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "cropService", fallbackMethod = "cropInventoryFallback")
    @io.github.resilience4j.retry.annotation.Retry(name = "cropService")
    public void reserveCropInventoryWithCircuitBreaker(Long cropId, Double quantityKg) {
        webClientBuilder.build().post()
                .uri(cropServiceUrl + "/api/v1/crops/inventory/reserve")
                .bodyValue(Map.of("cropId", cropId, "quantityKg", quantityKg))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void cropInventoryFallback(Long cropId, Double quantityKg, Throwable ex) {
        log.warn("Resilience4j Circuit Breaker / Retry triggered for crop-service reservation. CropId: {}, Quantity: {}, Error: {}",
                cropId, quantityKg, ex.getMessage());
        throw new RuntimeException("Crop Service is currently unavailable or inventory reservation timed out. Error: " + ex.getMessage(), ex);
    }

    private OrderResponse mapToResponse(CropOrder o) {
        return OrderResponse.builder()
                .orderId(o.getId())
                .dealerId(o.getDealerId())
                .farmerId(o.getFarmerId())
                .cropId(o.getCropId())
                .cropName(o.getCropName())
                .quantityKg(o.getQuantityKg())
                .pricePerKg(o.getPricePerKg())
                .totalAmount(o.getTotalAmount())
                .paymentId(o.getPaymentId())
                .deliveryId(o.getDeliveryId())
                .orderType(o.getOrderType())
                .status(o.getStatus())
                .createdAt(o.getCreatedAt())
                .build();
    }
}