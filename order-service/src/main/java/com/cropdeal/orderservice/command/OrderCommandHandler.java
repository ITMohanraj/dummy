package com.cropdeal.orderservice.command;

import com.cropdeal.orderservice.dto.OrderResponse;
import com.cropdeal.orderservice.service.OrderSagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCommandHandler {

    private final OrderSagaOrchestrator sagaOrchestrator;

    public OrderResponse handle(CreateOrderCommand command) {
        log.info("CQRS CommandHandler: Executing CreateOrderCommand for cropId: {}", command.getPurchaseRequest().getCropId());
        return sagaOrchestrator.executePurchaseSaga(command.getPurchaseRequest());
    }

    public OrderResponse handle(UpdateOrderStatusCommand command) {
        log.info("CQRS CommandHandler: Executing UpdateOrderStatusCommand for orderId: {} -> status: {}",
                command.getOrderId(), command.getStatus());
        return sagaOrchestrator.updateOrderStatus(command.getOrderId(), command.getStatus());
    }
}
