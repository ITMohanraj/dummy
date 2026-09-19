package com.cropdeal.orderservice.command;

import com.cropdeal.orderservice.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusCommand {
    private Long orderId;
    private OrderStatus status;
}
