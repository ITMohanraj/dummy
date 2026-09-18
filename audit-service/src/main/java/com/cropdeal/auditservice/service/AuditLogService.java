package com.cropdeal.auditservice.service;

import com.cropdeal.auditservice.entity.AuditLog;
import com.cropdeal.auditservice.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditRepo;

    @Transactional
    public AuditLog recordAudit(Long actorUserId, String actorRole, String action, String entityType, String entityId, String desc, String correlationId) {
        AuditLog logEntry = AuditLog.builder()
                .actorUserId(actorUserId)
                .actorRole(actorRole != null ? actorRole : "SYSTEM")
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(desc)
                .correlationId(correlationId)
                .build();
        return auditRepo.save(logEntry);
    }

    public List<AuditLog> getAllLogs() {
        return auditRepo.findAll();
    }

    public List<AuditLog> getLogsByAction(String action) {
        return auditRepo.findByActionOrderByTimestampDesc(action);
    }
}
