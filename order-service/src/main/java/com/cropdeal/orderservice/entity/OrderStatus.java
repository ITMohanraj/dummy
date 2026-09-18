package com.cropdeal.orderservice.entity;

public enum OrderStatus {
    PENDING,
    PRICE_AGREED,
    PAYMENT_PENDING,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    CONFIRMED,
    SELF_PICKUP,
    DELIVERY_REQUESTED,
    DELIVERY_ASSIGNED,
    PICKED_UP,
    IN_TRANSIT,
    DELIVERED,
    COMPLETED,
    CANCELLED,
    REFUNDED
}
