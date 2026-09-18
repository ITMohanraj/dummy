package com.cropdeal.auditservice.repository;

import com.cropdeal.auditservice.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByActorRoleOrderByTimestampDesc(String actorRole);
    List<AuditLog> findByActionOrderByTimestampDesc(String action);
    List<AuditLog> findByEntityTypeOrderByTimestampDesc(String entityType);
}
