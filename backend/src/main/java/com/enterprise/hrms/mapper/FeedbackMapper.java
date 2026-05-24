package com.enterprise.hrms.mapper;

import com.enterprise.hrms.dto.FeedbackDto;
import com.enterprise.hrms.entity.Feedback;
import org.springframework.stereotype.Component;

@Component
public class FeedbackMapper {

    public FeedbackDto toDto(Feedback feedback) {
        if (feedback == null) {
            return null;
        }

        FeedbackDto dto = new FeedbackDto();
        dto.setId(feedback.getId());
        dto.setName(feedback.getName());
        dto.setEmail(feedback.getEmail());
        dto.setSubject(feedback.getSubject());
        dto.setMessage(feedback.getMessage());
        dto.setRead(feedback.isRead());
        dto.setCreatedAt(feedback.getCreatedAt());

        return dto;
    }

    public Feedback toEntity(FeedbackDto dto) {
        if (dto == null) {
            return null;
        }

        Feedback feedback = new Feedback();
        feedback.setId(dto.getId());
        feedback.setName(dto.getName());
        feedback.setEmail(dto.getEmail());
        feedback.setSubject(dto.getSubject());
        feedback.setMessage(dto.getMessage());
        feedback.setRead(dto.isRead());

        return feedback;
    }
}
