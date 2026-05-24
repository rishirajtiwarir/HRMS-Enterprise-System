package com.enterprise.hrms.service.impl;

import com.enterprise.hrms.service.AlertNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AlertNotificationServiceImpl implements AlertNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(AlertNotificationServiceImpl.class);

    @Override
    public void sendEmailAlert(String to, String subject, String body) {
        logger.info("========================================= EMAIL DISPATCH ALERT =========================================");
        logger.info("TO: {}", to);
        logger.info("SUBJECT: {}", subject);
        logger.info("BODY CONTENT:\n{}", body);
        logger.info("========================================================================================================");
    }

    @Override
    public void triggerCallAlert(String phoneNumber, String message) {
        logger.info("========================================= OUTBOUND VOICE/SMS CALL ALERT =========================================");
        logger.info("TARGET PHONE NUMBER: {}", phoneNumber);
        logger.info("ALERT AUDIO MESSAGE: \"{}\"", message);
        logger.info("=================================================================================================================");
    }
}
