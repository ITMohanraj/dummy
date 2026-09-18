package com.cropdeal.invoiceservice.listener;

import com.cropdeal.invoiceservice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final InvoiceService invoiceService;

    @RabbitListener(queues = "invoice.order.events.queue")
    public void handleOrderEvents(Map<String, Object> payload) {
        log.info("Invoice Service received Order event: {}", payload);
        String status = payload.get("status") != null ? payload.get("status").toString() : "";

        if ("CONFIRMED".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status) || payload.containsKey("totalAmount")) {
            try {
                invoiceService.generateInvoiceFromOrder(payload);
            } catch (Exception e) {
                log.error("Failed to process invoice generation from order event: {}", e.getMessage());
            }
        }
    }
}