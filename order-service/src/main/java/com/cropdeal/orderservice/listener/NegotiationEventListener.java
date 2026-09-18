package com.cropdeal.orderservice.listener;

import com.cropdeal.orderservice.dto.PurchaseRequest;
import com.cropdeal.orderservice.service.OrderSagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NegotiationEventListener {

    private final OrderSagaOrchestrator sagaOrchestrator;

    @RabbitListener(queues = "order.negotiation.queue")
    public void handleNegotiationAccepted(Map<String, Object> payload) {
        log.info("Order Service received NegotiationAcceptedEvent: {}", payload);

        try {
            Long dealerId = Long.valueOf(payload.get("dealerId").toString());
            Long farmerId = Long.valueOf(payload.get("farmerId").toString());
            Long cropId = Long.valueOf(payload.get("cropListingId") != null ? payload.get("cropListingId").toString() : payload.get("cropId").toString());
            Double quantityKg = Double.valueOf(payload.get("quantityKg").toString());
            BigDecimal acceptedPrice = new BigDecimal(payload.get("acceptedPricePerKg").toString());

            PurchaseRequest purchase = PurchaseRequest.builder()
                    .dealerId(dealerId)
                    .farmerId(farmerId)
                    .cropId(cropId)
                    .cropName("Negotiated Crop #" + cropId)
                    .quantityKg(quantityKg)
                    .pricePerKg(acceptedPrice)
                    .paymentMethod("WALLET")
                    .build();

            sagaOrchestrator.executePurchaseSaga(purchase);
            log.info("Successfully executed Purchase Saga for accepted negotiation (Crop #{} @ ₹{}/KG)", cropId, acceptedPrice);
        } catch (Exception e) {
            log.error("Failed to process NegotiationAcceptedEvent in Order Service: {}", e.getMessage(), e);
        }
    }
}