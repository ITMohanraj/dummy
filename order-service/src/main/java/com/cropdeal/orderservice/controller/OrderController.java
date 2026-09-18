package com.cropdeal.orderservice.controller;

import com.cropdeal.orderservice.dto.OrderResponse;
import com.cropdeal.orderservice.dto.PurchaseRequest;
import com.cropdeal.orderservice.entity.OrderStatus;
import com.cropdeal.orderservice.service.OrderSagaOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders & Purchase Saga", description = "Order Lifecycle & Direct Checkout APIs")
public class OrderController {

    private final OrderSagaOrchestrator sagaOrchestrator;

    @PostMapping("/purchase")
    @Operation(summary = "Direct Crop Purchase Saga with inventory locking & payment")
    public ResponseEntity<OrderResponse> purchaseCrop(@Valid @RequestBody PurchaseRequest request) {
        OrderResponse response = sagaOrchestrator.executePurchaseSaga(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{orderId}/status")
    @Operation(summary = "Update order lifecycle state")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable("orderId") Long orderId,
            @RequestParam("status") OrderStatus status) {
        return ResponseEntity.ok(sagaOrchestrator.updateOrderStatus(orderId, status));
    }
}
