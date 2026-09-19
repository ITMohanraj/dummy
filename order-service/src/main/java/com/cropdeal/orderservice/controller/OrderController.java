package com.cropdeal.orderservice.controller;

import com.cropdeal.orderservice.command.CreateOrderCommand;
import com.cropdeal.orderservice.command.OrderCommandHandler;
import com.cropdeal.orderservice.command.UpdateOrderStatusCommand;
import com.cropdeal.orderservice.dto.OrderResponse;
import com.cropdeal.orderservice.dto.PurchaseRequest;
import com.cropdeal.orderservice.entity.OrderStatus;
import com.cropdeal.orderservice.query.GetOrderByIdQuery;
import com.cropdeal.orderservice.query.GetOrdersByDealerQuery;
import com.cropdeal.orderservice.query.GetOrdersByFarmerQuery;
import com.cropdeal.orderservice.query.OrderQueryHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders & CQRS Purchase Saga", description = "Order CQRS Commands & Queries APIs")
public class OrderController {

    private final OrderCommandHandler commandHandler;
    private final OrderQueryHandler queryHandler;

    // --- CQRS Commands (State Mutations) ---

    @PostMapping("/purchase")
    @Operation(summary = "CQRS Command: Direct Crop Purchase Saga with inventory locking & payment")
    public ResponseEntity<OrderResponse> purchaseCrop(@Valid @RequestBody PurchaseRequest request) {
        CreateOrderCommand command = CreateOrderCommand.builder()
                .purchaseRequest(request)
                .build();
        OrderResponse response = commandHandler.handle(command);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{orderId}/status")
    @Operation(summary = "CQRS Command: Update order lifecycle state")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable("orderId") Long orderId,
            @RequestParam("status") OrderStatus status) {
        UpdateOrderStatusCommand command = UpdateOrderStatusCommand.builder()
                .orderId(orderId)
                .status(status)
                .build();
        return ResponseEntity.ok(commandHandler.handle(command));
    }

    // --- CQRS Queries (Optimized Read Projections) ---

    @GetMapping("/{orderId}")
    @Operation(summary = "CQRS Query: Get order details by ID")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(queryHandler.handle(new GetOrderByIdQuery(orderId)));
    }

    @GetMapping("/dealer/{dealerId}")
    @Operation(summary = "CQRS Query: Get all orders for a dealer")
    public ResponseEntity<List<OrderResponse>> getOrdersByDealer(@PathVariable("dealerId") Long dealerId) {
        return ResponseEntity.ok(queryHandler.handle(new GetOrdersByDealerQuery(dealerId)));
    }

    @GetMapping("/farmer/{farmerId}")
    @Operation(summary = "CQRS Query: Get all orders for a farmer")
    public ResponseEntity<List<OrderResponse>> getOrdersByFarmer(@PathVariable("farmerId") Long farmerId) {
        return ResponseEntity.ok(queryHandler.handle(new GetOrdersByFarmerQuery(farmerId)));
    }
}
