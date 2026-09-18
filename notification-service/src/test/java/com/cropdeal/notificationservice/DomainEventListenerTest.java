package com.cropdeal.notificationservice;

import com.cropdeal.notificationservice.listener.DomainEventListener;
import com.cropdeal.notificationservice.service.EmailService;
import com.cropdeal.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DomainEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private DomainEventListener domainEventListener;

    @Test
    void testHandlePasswordResetOtpEvent() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "PASSWORD_RESET_OTP");
        payload.put("userId", 101L);
        payload.put("email", "farmer@gmail.com");
        payload.put("otp", "654321");
        payload.put("expiresInMinutes", 5);

        domainEventListener.handlePasswordResetOtpEvent(payload);

        verify(emailService, times(1)).sendPasswordResetOtp("farmer@gmail.com", "654321", 5);
        verify(notificationService, times(1)).saveNotification(
                eq(101L), eq("USER"), eq("Password Reset OTP Generated"), anyString(), eq("SECURITY")
        );
    }
}
