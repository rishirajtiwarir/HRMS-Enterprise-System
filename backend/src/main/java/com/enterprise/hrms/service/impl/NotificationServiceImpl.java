package com.enterprise.hrms.service.impl;

import com.enterprise.hrms.dto.NotificationDto;
import com.enterprise.hrms.entity.Employee;
import com.enterprise.hrms.entity.Notification;
import com.enterprise.hrms.exception.ResourceNotFoundException;
import com.enterprise.hrms.mapper.NotificationMapper;
import com.enterprise.hrms.repository.EmployeeRepository;
import com.enterprise.hrms.repository.NotificationRepository;
import com.enterprise.hrms.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotifications(Long employeeId) {
        return notificationRepository.findByEmployeeIdAndReadStatusOrderByCreatedAtDesc(employeeId, false).stream()
                .map(notificationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getAllNotifications(Long employeeId) {
        return notificationRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId).stream()
                .map(notificationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));
        notification.setReadStatus(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long employeeId) {
        List<Notification> unread = notificationRepository.findByEmployeeIdAndReadStatusOrderByCreatedAtDesc(employeeId, false);
        unread.forEach(notification -> notification.setReadStatus(true));
        notificationRepository.saveAll(unread);
    }

    @Override
    @Transactional
    public NotificationDto sendNotification(Long employeeId, String title, String message, String type) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        Notification notification = new Notification();
        notification.setEmployee(employee);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type != null ? type : "INFO");
        notification.setReadStatus(false);

        Notification savedNotification = notificationRepository.save(notification);
        return notificationMapper.toDto(savedNotification);
    }
}
