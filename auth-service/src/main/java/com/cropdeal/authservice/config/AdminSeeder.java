package com.cropdeal.authservice.config;

import com.cropdeal.authservice.entity.AccountStatus;
import com.cropdeal.authservice.entity.Role;
import com.cropdeal.authservice.entity.UserAuth;
import com.cropdeal.authservice.repository.UserAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = "mohanraj.k1110@gmail.com";
        if (!userAuthRepository.existsByEmail(adminEmail)) {
            log.info("Creating default initial administrator: {}", adminEmail);
            UserAuth admin = UserAuth.builder()
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .role(Role.ROLE_ADMIN)
                    .fullName("Mohanraj K (Super Admin)")
                    .phone("+91-9876543210")
                    .status(AccountStatus.ACTIVE)
                    .build();
            userAuthRepository.save(admin);
            log.info("Default administrator created successfully.");
        } else {
            log.info("Default administrator already exists.");
        }
    }
}
