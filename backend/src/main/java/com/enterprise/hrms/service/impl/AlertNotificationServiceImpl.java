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
                logger.info("✅ EMAIL SENT SUCCESSFULLY to {} via Gmail SMTP.", to);
            } catch (Exception e) {
                logger.error("❌ SMTP EMAIL FAILED — Error: {}", e.getMessage());
                if (e.getCause() != null) {
                    logger.error("❌ Root cause: {}", e.getCause().getMessage());
                    if (e.getCause().getCause() != null) {
                        logger.error("❌ Deep cause: {}", e.getCause().getCause().getMessage());
                    }
                }
                logger.error("❌ ACTION REQUIRED: Check Gmail App Password & 2-Step Verification at https://myaccount.google.com/apppasswords");
            }
        } else {
            logger.warn("⚠️ JavaMailSender bean is null — spring-boot-starter-mail may not be configured properly.");
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
