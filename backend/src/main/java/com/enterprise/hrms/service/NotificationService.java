package com.enterprise.hrms.service;

import com.enterprise.hrms.dto.NotificationDto;
import java.util.List;

public interface NotificationService {
    List<NotificationDto> getUnreadNotifications(Long employeeId);
    List<NotificationDto> getAllNotifications(Long employeeId);
    void markAsRead(Long notificationId);
    void markAllAsRead(Long employeeId);
    NotificationDto sendNotification(Long employeeId, String title, String message, String type);
}
