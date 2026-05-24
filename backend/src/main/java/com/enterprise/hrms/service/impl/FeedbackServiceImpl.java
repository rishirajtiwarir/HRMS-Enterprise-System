package com.enterprise.hrms.service.impl;

import com.enterprise.hrms.dto.FeedbackDto;
import com.enterprise.hrms.entity.Feedback;
import com.enterprise.hrms.exception.ResourceNotFoundException;
import com.enterprise.hrms.mapper.FeedbackMapper;
import com.enterprise.hrms.repository.FeedbackRepository;
import com.enterprise.hrms.service.AlertNotificationService;
import com.enterprise.hrms.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Autowired
    private AlertNotificationService alertNotificationService;

    @Override
    @Transactional
    public FeedbackDto submitFeedback(FeedbackDto feedbackDto) {
        Feedback feedback = feedbackMapper.toEntity(feedbackDto);
        feedback.setRead(false);
        Feedback savedFeedback = feedbackRepository.save(feedback);

        // Prepare email body
        String emailSubject = "HRMS System Alert: New Feedback Received - " + savedFeedback.getSubject();
        String emailBody = String.format(
                "Hello,\n\nA new feedback message has been submitted on the HRMS Portal.\n\n" +
                "-----------------------------------------\n" +
                "SENDER NAME: %s\n" +
                "SENDER EMAIL: %s\n" +
                "SUBJECT: %s\n" +
                "MESSAGE:\n%s\n" +
                "-----------------------------------------\n\n" +
                "This message has been saved in the system databases. Please log into the Admin panel to review it.",
                savedFeedback.getName(),
                savedFeedback.getEmail(),
                savedFeedback.getSubject(),
                savedFeedback.getMessage()
        );

        // 1. Dispatch Email Alert
        alertNotificationService.sendEmailAlert("rishiraj2004tiwari@gmail.com", emailSubject, emailBody);

        // 2. Dispatch Call Alert
        String callMessage = String.format(
                "Alert. New feedback received on your HRMS portal from %s regarding %s.",
                savedFeedback.getName(),
                savedFeedback.getSubject()
        );
        alertNotificationService.triggerCallAlert("8103425522", callMessage);

        return feedbackMapper.toDto(savedFeedback);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackDto> getAllFeedbacks() {
        return feedbackRepository.findAll().stream()
                .map(feedbackMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FeedbackDto markAsRead(Long id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found with ID: " + id));
        feedback.setRead(true);
        Feedback saved = feedbackRepository.save(feedback);
        return feedbackMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteFeedback(Long id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found with ID: " + id));
        feedbackRepository.delete(feedback);
    }
}
