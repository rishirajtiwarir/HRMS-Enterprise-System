package com.enterprise.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing notifications.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private String title;
    private String message;
    private String type;
    private Boolean readStatus;
    private Long employeeId;
    private LocalDateTime createdAt;
}
