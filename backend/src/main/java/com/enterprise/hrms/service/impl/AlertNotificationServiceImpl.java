package com.enterprise.hrms.service.impl;

import com.enterprise.hrms.service.AlertNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlertNotificationServiceImpl implements AlertNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(AlertNotificationServiceImpl.class);

    private static final String RESEND_API_KEY = "re_8v7t13cN_Arwx1WdW3PJbKowHzyJb7nyo";
    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    @Override
    public void sendEmailAlert(String to, String subject, String body) {
        logger.info("========================================= EMAIL DISPATCH ALERT (RESEND API) =========================================");
        logger.info("TO: {}", to);
        logger.info("SUBJECT: {}", subject);
        logger.info("BODY CONTENT:\n{}", body);
        logger.info("=====================================================================================================================");

        try {
            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(RESEND_API_KEY);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("from", "HRMS System <onboarding@resend.dev>");
            requestBody.put("to", List.of(to));
            requestBody.put("subject", subject);
            requestBody.put("text", body);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(RESEND_API_URL, HttpMethod.POST, requestEntity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("✅ EMAIL SENT SUCCESSFULLY to {} via Resend API. Response: {}", to, response.getBody());
            } else {
                logger.error("❌ RESEND API EMAIL FAILED. Status: {}, Response: {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            logger.error("❌ ERROR DURING RESEND API CALL: {}", e.getMessage(), e);
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
