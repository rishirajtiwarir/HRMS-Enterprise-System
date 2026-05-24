package com.enterprise.hrms.service;

public interface AlertNotificationService {
    void sendEmailAlert(String to, String subject, String body);
    void triggerCallAlert(String phoneNumber, String message);
}
