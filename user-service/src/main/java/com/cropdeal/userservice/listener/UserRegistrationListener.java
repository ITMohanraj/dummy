package com.cropdeal.userservice.listener;

import com.cropdeal.userservice.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationListener {

    private final UserProfileService profileService;

    @RabbitListener(queues = "user.registration.queue")
    public void handleUserRegistered(Map<String, Object> payload) {
        log.info("User Service received user.registered event: {}", payload);

        try {
            Long userId = payload.get("userId") != null ? Long.valueOf(payload.get("userId").toString()) : null;
            String email = (String) payload.get("email");
            String fullName = (String) payload.get("fullName");
            String phone = (String) payload.get("phone");
            String role = (String) payload.get("role");

            if (userId != null && role != null) {
                profileService.createInitialProfile(userId, email, fullName, phone, role);
                log.info("Initialized profile for User ID: {} (Role: {})", userId, role);
            }
        } catch (Exception e) {
            log.error("Failed to process user.registered event in User Service: {}", e.getMessage(), e);
        }
    }
}