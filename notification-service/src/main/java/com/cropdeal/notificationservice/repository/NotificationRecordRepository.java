package com.cropdeal.notificationservice.repository;

import com.cropdeal.notificationservice.entity.NotificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRecordRepository extends JpaRepository<NotificationRecord, Long> {
    List<NotificationRecord> findByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId);
    long countByRecipientUserIdAndIsReadFalse(Long recipientUserId);
}
