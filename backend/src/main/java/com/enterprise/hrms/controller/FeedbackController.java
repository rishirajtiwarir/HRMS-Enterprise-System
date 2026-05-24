package com.enterprise.hrms.controller;

import com.enterprise.hrms.dto.FeedbackDto;
import com.enterprise.hrms.dto.MessageResponse;
import com.enterprise.hrms.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    // Public endpoint for feedback/support submission
    @PostMapping
    public ResponseEntity<FeedbackDto> submitFeedback(@Valid @RequestBody FeedbackDto feedbackDto) {
        FeedbackDto saved = feedbackService.submitFeedback(feedbackDto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // Secured endpoint: Retrieve all feedbacks (ADMIN or HR only)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<List<FeedbackDto>> getAllFeedbacks() {
        List<FeedbackDto> list = feedbackService.getAllFeedbacks();
        return ResponseEntity.ok(list);
    }

    // Secured endpoint: Mark feedback as read (ADMIN or HR only)
    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<FeedbackDto> markAsRead(@PathVariable Long id) {
        FeedbackDto updated = feedbackService.markAsRead(id);
        return ResponseEntity.ok(updated);
    }

    // Secured endpoint: Delete feedback record (ADMIN or HR only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.ok(new MessageResponse("Feedback message deleted successfully."));
    }
}
