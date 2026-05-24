package com.enterprise.hrms.controller;

import com.enterprise.hrms.dto.LeaveRequestDto;
import com.enterprise.hrms.dto.MessageResponse;
import com.enterprise.hrms.entity.LeaveStatus;
import com.enterprise.hrms.exception.BadRequestException;
import com.enterprise.hrms.security.UserPrincipal;
import com.enterprise.hrms.service.LeaveRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller managing employee leave requests and balances.
 */
@RestController
@RequestMapping("/leaves")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    // Apply for leave (Authenticated employee)
    @PostMapping("/request")
    public ResponseEntity<LeaveRequestDto> applyLeave(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody LeaveRequestDto leaveRequestDto) {
        
        if (currentUser.getEmployeeId() == null) {
            throw new BadRequestException("Authenticated user has no employee profile associated to apply for leave.");
        }
        
        // Force the requester's employeeId for security
        leaveRequestDto.setEmployeeId(currentUser.getEmployeeId());
        
        LeaveRequestDto createdRequest = leaveRequestService.applyLeave(leaveRequestDto);
        return ResponseEntity.ok(createdRequest);
    }

    // Resolve leave request: APPROVE or REJECT (ADMIN or HR only)
    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<LeaveRequestDto> resolveLeave(
            @PathVariable Long id,
            @RequestParam("status") LeaveStatus status,
            @RequestParam(value = "rejectionReason", required = false) String rejectionReason) {
        
        if (status == LeaveStatus.REJECTED && (rejectionReason == null || rejectionReason.trim().isEmpty())) {
            throw new BadRequestException("Rejection reason is required when rejecting leave requests.");
        }

        LeaveRequestDto resolvedRequest = leaveRequestService.resolveLeave(id, status, rejectionReason);
        return ResponseEntity.ok(resolvedRequest);
    }

    // Get leave balances for an employee (ADMIN, HR, or self)
    @GetMapping("/employee/{employeeId}/balances")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or #employeeId == authentication.principal.employeeId")
    public ResponseEntity<Map<String, Integer>> getLeaveBalances(@PathVariable Long employeeId) {
        Map<String, Integer> balances = leaveRequestService.getLeaveBalances(employeeId);
        return ResponseEntity.ok(balances);
    }

    // Get leave history for a specific employee (ADMIN, HR, or self)
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or #employeeId == authentication.principal.employeeId")
    public ResponseEntity<List<LeaveRequestDto>> getEmployeeLeaveHistory(@PathVariable Long employeeId) {
        List<LeaveRequestDto> history = leaveRequestService.getEmployeeLeaveHistory(employeeId);
        return ResponseEntity.ok(history);
    }

    // Get all leave requests (ADMIN or HR only)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<List<LeaveRequestDto>> getAllLeaveRequests() {
        List<LeaveRequestDto> requests = leaveRequestService.getAllLeaveRequests();
        return ResponseEntity.ok(requests);
    }

    // Cancel a leave request (Self only)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<LeaveRequestDto> cancelLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        LeaveRequestDto record = leaveRequestService.getAllLeaveRequests().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new com.enterprise.hrms.exception.ResourceNotFoundException("Leave request not found with ID: " + id));
        
        if (currentUser.getEmployeeId() == null || !currentUser.getEmployeeId().equals(record.getEmployeeId())) {
            throw new BadRequestException("Access denied: You can only cancel your own leave requests.");
        }
        
        LeaveRequestDto resolved = leaveRequestService.cancelLeave(id);
        return ResponseEntity.ok(resolved);
    }
}
