package com.cropdeal.notificationservice.service;

import com.cropdeal.notificationservice.entity.NotificationRecord;
import com.cropdeal.notificationservice.repository.NotificationRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRecordRepository notifRepo;

    public void saveNotification(Long recipientUserId, String role, String title, String message, String type) {
        NotificationRecord record = NotificationRecord.builder()
                .recipientUserId(recipientUserId)
                .role(role)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();
        notifRepo.save(record);
        log.info("Saved notification for User ID: {}, Type: {}", recipientUserId, type);
    }

    public List<NotificationRecord> getUserNotifications(Long userId) {
        return notifRepo.findByRecipientUserIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notifRepo.countByRecipientUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notifRepo.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notifRepo.save(n);
        });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<NotificationRecord> list = notifRepo.findByRecipientUserIdOrderByCreatedAtDesc(userId);
        list.forEach(n -> n.setRead(true));
        notifRepo.saveAll(list);
    }
}
