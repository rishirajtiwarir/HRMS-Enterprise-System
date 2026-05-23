package com.enterprise.hrms.mapper;

import com.enterprise.hrms.dto.NotificationDto;
import com.enterprise.hrms.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationDto toDto(Notification notification) {
        if (notification == null) {
            return null;
        }

        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setReadStatus(notification.getReadStatus());
        dto.setCreatedAt(notification.getCreatedAt());

        if (notification.getEmployee() != null) {
            dto.setEmployeeId(notification.getEmployee().getId());
        }

        return dto;
    }

    public Notification toEntity(NotificationDto dto) {
        if (dto == null) {
            return null;
        }

        Notification notification = new Notification();
        notification.setId(dto.getId());
        notification.setTitle(dto.getTitle());
        notification.setMessage(dto.getMessage());
        notification.setType(dto.getType());
        notification.setReadStatus(dto.getReadStatus());

        return notification;
    }
}
