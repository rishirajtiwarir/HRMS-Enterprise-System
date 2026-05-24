package com.enterprise.hrms.service;

import com.enterprise.hrms.dto.FeedbackDto;
import java.util.List;

public interface FeedbackService {
    FeedbackDto submitFeedback(FeedbackDto feedbackDto);
    List<FeedbackDto> getAllFeedbacks();
    FeedbackDto markAsRead(Long id);
    void deleteFeedback(Long id);
}
