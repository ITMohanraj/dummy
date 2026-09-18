package com.cropdeal.notificationservice.listener;

import com.cropdeal.notificationservice.service.EmailService;
import com.cropdeal.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventListener {

    private final NotificationService notificationService;
    private final EmailService emailService;

    @RabbitListener(queues = "${cropdeal.rabbitmq.queue.order:order.notification.queue}")
    public void handleOrderEvent(Map<String, Object> payload) {
        log.info("Notification Service received Order event: {}", payload);
        Long dealerId = payload.get("dealerId") != null ? Long.valueOf(payload.get("dealerId").toString()) : 1L;
        Long orderId = payload.get("orderId") != null ? Long.valueOf(payload.get("orderId").toString()) : 1L;

        notificationService.saveNotification(dealerId, "DEALER", "Order Update",
                "Your order #" + orderId + " status is: " + payload.get("status"), "ORDER");
    }

    @RabbitListener(queues = "${cropdeal.rabbitmq.queue.password-reset:notification.password-reset.queue}")
    public void handlePasswordResetOtpEvent(Map<String, Object> payload) {
        log.info("Notification Service received Password Reset OTP event for email: {}", payload.get("email"));

        String email = (String) payload.get("email");
        String otp = (String) payload.get("otp");
        Long userId = payload.get("userId") != null ? Long.valueOf(payload.get("userId").toString()) : null;
        int expiresInMinutes = payload.get("expiresInMinutes") != null ?
                Integer.parseInt(payload.get("expiresInMinutes").toString()) : 5;

        if (email != null && otp != null) {
            // Send email via Gmail SMTP
            emailService.sendPasswordResetOtp(email, otp, expiresInMinutes);

            // Persist in-app security notification
            if (userId != null) {
                notificationService.saveNotification(
                        userId,
                        "USER",
                        "Password Reset OTP Generated",
                        "A 6-digit password reset OTP was sent to your registered email (" + email + ").",
                        "SECURITY"
                );
            }
        }
    }

    @RabbitListener(queues = "negotiation.notification.queue")
    public void handleNegotiationEvent(Map<String, Object> payload) {
        log.info("Notification Service received Negotiation event: {}", payload);
        Long farmerId = payload.get("farmerId") != null ? Long.valueOf(payload.get("farmerId").toString()) : null;
        Long dealerId = payload.get("dealerId") != null ? Long.valueOf(payload.get("dealerId").toString()) : null;
        Long negId = payload.get("negotiationId") != null ? Long.valueOf(payload.get("negotiationId").toString()) : null;
        String status = payload.get("status") != null ? payload.get("status").toString() : "UPDATED";

        if (farmerId != null) {
            notificationService.saveNotification(
                    farmerId,
                    "FARMER",
                    "Negotiation Update",
                    "Negotiation #" + negId + " has status: " + status + " for Crop #" + payload.get("cropId"),
                    "NEGOTIATION"
            );
        }
        if (dealerId != null && ("COUNTERED".equalsIgnoreCase(status) || "ACCEPTED".equalsIgnoreCase(status) || "REJECTED".equalsIgnoreCase(status))) {
            notificationService.saveNotification(
                    dealerId,
                    "DEALER",
                    "Negotiation " + status,
                    "Farmer " + status + " your negotiation #" + negId + " for Crop #" + payload.get("cropId"),
                    "NEGOTIATION"
            );
        }
    }

    @RabbitListener(queues = "invoice.notification.queue")
    public void handleInvoiceEvent(Map<String, Object> payload) {
        log.info("Notification Service received Invoice event: {}", payload);
        Long dealerId = payload.get("dealerId") != null ? Long.valueOf(payload.get("dealerId").toString()) : null;
        Long farmerId = payload.get("farmerId") != null ? Long.valueOf(payload.get("farmerId").toString()) : null;
        String invNum = payload.get("invoiceNumber") != null ? payload.get("invoiceNumber").toString() : "N/A";
        Long orderId = payload.get("orderId") != null ? Long.valueOf(payload.get("orderId").toString()) : null;

        if (dealerId != null) {
            notificationService.saveNotification(
                    dealerId,
                    "DEALER",
                    "Tax Invoice Generated",
                    "Invoice #" + invNum + " for Order #" + orderId + " has been generated and is ready for pickup verification.",
                    "INVOICE"
            );
        }
        if (farmerId != null) {
            notificationService.saveNotification(
                    farmerId,
                    "FARMER",
                    "Farmer Payment Receipt Ready",
                    "Payment receipt for Order #" + orderId + " (Invoice #" + invNum + ") has been confirmed.",
                    "PAYMENT"
            );
        }
    }
}
