package com.cropdeal.notificationservice.controller;

import com.cropdeal.notificationservice.entity.NotificationRecord;
import com.cropdeal.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User Notification Alerts & Unread Badges APIs")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all notifications for a user")
    public ResponseEntity<List<NotificationRecord>> getUserNotifications(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @GetMapping("/user/{userId}/unread-count")
    @Operation(summary = "Get total unread notifications count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(@PathVariable("userId") Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("userId", userId, "unreadCount", count));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Mark a single notification as read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable("notificationId") Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(Map.of("notificationId", notificationId, "status", "READ"));
    }

    @PatchMapping("/user/{userId}/read-all")
    @Operation(summary = "Mark all user notifications as read")
    public ResponseEntity<Map<String, Object>> markAllAsRead(@PathVariable("userId") Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("userId", userId, "status", "ALL_READ"));
    }
}
