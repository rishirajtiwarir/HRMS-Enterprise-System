package com.enterprise.hrms.service.impl;

import com.enterprise.hrms.service.AlertNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class AlertNotificationServiceImpl implements AlertNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(AlertNotificationServiceImpl.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Override
    public void sendEmailAlert(String to, String subject, String body) {
        logger.info("========================================= EMAIL DISPATCH ALERT =========================================");
        logger.info("TO: {}", to);
        logger.info("SUBJECT: {}", subject);
        logger.info("BODY CONTENT:\n{}", body);
        logger.info("========================================================================================================");

        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("rishiraj2004tiwari@gmail.com");
                message.setTo(to);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
                logger.info("Real email alert successfully sent to {} via JavaMailSender.", to);
            } catch (Exception e) {
                logger.error("Failed to send real email via SMTP: {}. Check credentials in application.properties.", e.getMessage());
            }
        } else {
            logger.warn("JavaMailSender is not initialized. Falling back to log simulation.");
        }
    }

    @Override
    public void triggerCallAlert(String phoneNumber, String message) {
        logger.info("========================================= OUTBOUND VOICE/SMS CALL ALERT =========================================");
        logger.info("TARGET PHONE NUMBER: {}", phoneNumber);
        logger.info("ALERT AUDIO MESSAGE: \"{}\"", message);
        logger.info("=================================================================================================================");
    }
}
