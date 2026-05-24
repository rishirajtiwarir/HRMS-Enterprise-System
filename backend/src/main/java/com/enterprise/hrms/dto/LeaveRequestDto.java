package com.enterprise.hrms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Data Transfer Object representing leave requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDto {
    private Long id;
    private Long employeeId;
    private String employeeName;

    @NotNull(message = "Leave type is required")
    private String leaveType; // SICK, CASUAL, VACATION, MATERNITY

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Reason is required")
    private String reason;

    private String status; // PENDING, APPROVED, REJECTED
    private String rejectionReason;
    private String sickLeaveDocumentUrl;
    private Integer stage;
    private String currentApproverRole;
    private Boolean cancelled;
    private LocalDate createdAt;
}
