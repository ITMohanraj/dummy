package com.cropdeal.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:mohanraj.k1110@gmail.com}")
    private String senderEmail;

    public void sendPasswordResetOtp(String recipientEmail, String otp, int expiresInMinutes) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(recipientEmail);
            message.setSubject("CropDeal - Password Reset OTP");
            message.setText(
                "Hello,\n\n" +
                "You have requested to reset your password for your CropDeal account.\n" +
                "Your One-Time Password (OTP) is: " + otp + "\n\n" +
                "This OTP is valid for " + expiresInMinutes + " minutes. Do not share this OTP with anyone.\n\n" +
                "If you did not request this, please ignore this email or contact support.\n\n" +
                "Regards,\n" +
                "CropDeal Security Team"
            );

            mailSender.send(message);
            log.info("Password reset OTP email dispatched successfully to: {}", recipientEmail);
        } catch (Exception e) {
            log.warn("Could not dispatch email via SMTP (offline or credentials unconfigured): {}. Notification saved to database.", e.getMessage());
        }
    }
}
