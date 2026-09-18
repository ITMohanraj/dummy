package com.cropdeal.auditservice.listener;

import com.cropdeal.auditservice.entity.AuditLog;
import com.cropdeal.auditservice.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventListener {

    private final AuditLogRepository auditRepo;

    @RabbitListener(queues = "audit.events.queue")
    public void onAuditEvent(Map<String, Object> payload) {
        log.info("Audit Service captured event: {}", payload);
        try {
            AuditLog logEntry = AuditLog.builder()
                    .actorRole("SYSTEM")
                    .action(payload.getOrDefault("action", "EVENT_DISPATCHED").toString())
                    .entityType("DOMAIN_EVENT")
                    .entityId(payload.getOrDefault("id", "N/A").toString())
                    .description(payload.toString())
                    .build();
            auditRepo.save(logEntry);
        } catch (Exception e) {
            log.warn("Failed to persist audit log: {}", e.getMessage());
        }
    }
}
