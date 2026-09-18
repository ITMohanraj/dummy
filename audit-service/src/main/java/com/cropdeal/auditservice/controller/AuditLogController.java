package com.cropdeal.auditservice.controller;

import com.cropdeal.auditservice.entity.AuditLog;
import com.cropdeal.auditservice.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Trail", description = "System Audit Logs & Regulatory Compliance APIs")
public class AuditLogController {

    private final AuditLogService auditService;

    @GetMapping("/logs")
    @Operation(summary = "Get complete append-only audit trail logs")
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        return ResponseEntity.ok(auditService.getAllLogs());
    }

    @GetMapping("/logs/action/{action}")
    @Operation(summary = "Filter audit logs by specific business action")
    public ResponseEntity<List<AuditLog>> getLogsByAction(@PathVariable("action") String action) {
        return ResponseEntity.ok(auditService.getLogsByAction(action));
    }
}
