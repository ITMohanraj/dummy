package com.cropdeal.orderservice.query;

import com.cropdeal.orderservice.dto.OrderResponse;
import com.cropdeal.orderservice.entity.CropOrder;
import com.cropdeal.orderservice.repository.CropOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderQueryHandler {

    private final CropOrderRepository orderRepo;

    @Transactional(readOnly = true)
    public OrderResponse handle(GetOrderByIdQuery query) {
        log.info("CQRS QueryHandler: Executing GetOrderByIdQuery for orderId: {}", query.getOrderId());
        CropOrder order = orderRepo.findById(query.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + query.getOrderId()));
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> handle(GetOrdersByDealerQuery query) {
        log.info("CQRS QueryHandler: Executing GetOrdersByDealerQuery for dealerId: {}", query.getDealerId());
        return orderRepo.findByDealerId(query.getDealerId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> handle(GetOrdersByFarmerQuery query) {
        log.info("CQRS QueryHandler: Executing GetOrdersByFarmerQuery for farmerId: {}", query.getFarmerId());
        return orderRepo.findByFarmerId(query.getFarmerId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
