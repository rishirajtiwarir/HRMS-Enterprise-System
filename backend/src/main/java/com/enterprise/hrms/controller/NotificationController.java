package com.enterprise.hrms.controller;

import com.enterprise.hrms.dto.MessageResponse;
import com.enterprise.hrms.dto.NotificationDto;
import com.enterprise.hrms.exception.BadRequestException;
import com.enterprise.hrms.security.UserPrincipal;
import com.enterprise.hrms.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller managing employee notifications and alerts.
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // Get unread notifications for current employee
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser.getEmployeeId() == null) {
            throw new BadRequestException("Authenticated user has no employee profile associated to view notifications.");
        }
        List<NotificationDto> notifications = notificationService.getUnreadNotifications(currentUser.getEmployeeId());
        return ResponseEntity.ok(notifications);
    }

    // Get all notifications for current employee
    @GetMapping
    public ResponseEntity<List<NotificationDto>> getAllNotifications(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser.getEmployeeId() == null) {
            throw new BadRequestException("Authenticated user has no employee profile associated to view notifications.");
        }
        List<NotificationDto> notifications = notificationService.getAllNotifications(currentUser.getEmployeeId());
        return ResponseEntity.ok(notifications);
    }

    // Mark specific notification as read
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(new MessageResponse("Notification marked as read."));
    }

    // Mark all notifications as read for current employee
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser.getEmployeeId() == null) {
            throw new BadRequestException("Authenticated user has no employee profile associated to clear notifications.");
        }
        notificationService.markAllAsRead(currentUser.getEmployeeId());
        return ResponseEntity.ok(new MessageResponse("All notifications marked as read."));
    }
}
