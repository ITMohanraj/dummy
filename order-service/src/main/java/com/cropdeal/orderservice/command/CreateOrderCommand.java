package com.cropdeal.orderservice.command;

import com.cropdeal.orderservice.dto.PurchaseRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderCommand {
    private PurchaseRequest purchaseRequest;
}
