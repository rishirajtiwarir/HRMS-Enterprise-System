package com.enterprise.hrms.service.impl;

import com.enterprise.hrms.dto.LeaveRequestDto;
import com.enterprise.hrms.entity.*;
import com.enterprise.hrms.exception.BadRequestException;
import com.enterprise.hrms.exception.ResourceNotFoundException;
import com.enterprise.hrms.mapper.LeaveRequestMapper;
import com.enterprise.hrms.repository.EmployeeRepository;
import com.enterprise.hrms.repository.LeaveRequestRepository;
import com.enterprise.hrms.repository.NotificationRepository;
import com.enterprise.hrms.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private LeaveRequestMapper leaveRequestMapper;

    // Define yearly allocations for each type of leave
    private static final Map<LeaveType, Integer> LEAVE_ALLOCATIONS = Map.of(
            LeaveType.SICK, 12,
            LeaveType.CASUAL, 10,
            LeaveType.VACATION, 15,
            LeaveType.MATERNITY, 90
    );

    @Override
    @Transactional
    public LeaveRequestDto applyLeave(LeaveRequestDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + dto.getEmployeeId()));

        LeaveType leaveType = LeaveType.valueOf(dto.getLeaveType());
        long requestedDays = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;

        if (requestedDays <= 0) {
            throw new BadRequestException("End date must be after start date!");
        }

        // Check if balance is sufficient
        Map<String, Integer> balances = getLeaveBalances(dto.getEmployeeId());
        int currentBalance = balances.getOrDefault(leaveType.name(), 0);

        if (requestedDays > currentBalance) {
            throw new BadRequestException("Insufficient " + leaveType.name() + " leave balance! Requested: " 
                    + requestedDays + " days, Available: " + currentBalance + " days.");
        }

        LeaveRequest request = leaveRequestMapper.toEntity(dto);
        request.setEmployee(employee);
        request.setStatus(LeaveStatus.PENDING);
        
        // Setup initial approval stages
        if (employee.getManager() != null) {
            request.setStage(1);
            request.setCurrentApproverRole("ROLE_TEAM_LEAD");
        } else {
            request.setStage(2);
            request.setCurrentApproverRole("ROLE_HR");
        }
        request.setCancelled(false);
        request.setSickLeaveDocumentUrl(dto.getSickLeaveDocumentUrl());

        LeaveRequest savedRequest = leaveRequestRepository.save(request);

        // Generate notification for HR/Admin (can also write a simple notification entry for the employee)
        Notification notification = new Notification();
        notification.setTitle("New Leave Request");
        notification.setMessage("Employee " + employee.getFirstName() + " " + employee.getLastName() 
                + " has requested " + requestedDays + " days of " + leaveType.name() + " leave.");
        notification.setType("INFO");
        notification.setEmployee(employee);
        notificationRepository.save(notification);

        return leaveRequestMapper.toDto(savedRequest);
    }

    @Override
    @Transactional
    public LeaveRequestDto resolveLeave(Long id, LeaveStatus status, String rejectionReason) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + id));

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Leave request has already been processed!");
        }

        if (status == LeaveStatus.REJECTED) {
            request.setStatus(LeaveStatus.REJECTED);
            request.setRejectionReason(rejectionReason);
            
            Notification notification = new Notification();
            notification.setEmployee(request.getEmployee());
            notification.setTitle("Leave Request Rejected");
            notification.setMessage("Your leave request for " + request.getLeaveType().name() + " has been rejected. Reason: " + rejectionReason);
            notification.setType("ALERT");
            notificationRepository.save(notification);
        } else if (status == LeaveStatus.APPROVED) {
            // Multi-stage approval
            if (request.getStage() == 1) {
                request.setStage(2);
                request.setCurrentApproverRole("ROLE_HR");
                request.setStatus(LeaveStatus.PENDING);
                
                Notification notification = new Notification();
                notification.setEmployee(request.getEmployee());
                notification.setTitle("Leave Pending HR Approval");
                notification.setMessage("Your leave request has been approved by your Team Lead and is now pending final HR review.");
                notification.setType("INFO");
                notificationRepository.save(notification);
            } else {
                request.setStatus(LeaveStatus.APPROVED);
                
                Notification notification = new Notification();
                notification.setEmployee(request.getEmployee());
                notification.setTitle("Leave Request Fully Approved");
                notification.setMessage("Your leave request for " + request.getLeaveType().name() + " from " 
                        + request.getStartDate() + " to " + request.getEndDate() + " has been fully approved.");
                notification.setType("SUCCESS");
                notificationRepository.save(notification);
            }
        }

        LeaveRequest updatedRequest = leaveRequestRepository.save(request);
        return leaveRequestMapper.toDto(updatedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getEmployeeLeaveHistory(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId).stream()
                .map(leaveRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getAllLeaveRequests() {
        return leaveRequestRepository.findAll().stream()
                .map(leaveRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Integer> getLeaveBalances(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }

        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findByEmployeeId(employeeId).stream()
                .filter(req -> req.getStatus() == LeaveStatus.APPROVED && (req.getCancelled() == null || !req.getCancelled()))
                .collect(Collectors.toList());

        Map<String, Integer> balances = new HashMap<>();

        for (LeaveType type : LeaveType.values()) {
            int allocated = LEAVE_ALLOCATIONS.getOrDefault(type, 0);
            
            // Sum taken days
            long taken = approvedLeaves.stream()
                    .filter(req -> req.getLeaveType() == type)
                    .mapToLong(req -> ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1)
                    .sum();

            balances.put(type.name(), allocated - (int) taken);
        }

        return balances;
    }

    @Override
    @Transactional
    public LeaveRequestDto cancelLeave(Long id) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + id));
        
        request.setCancelled(true);
        request.setStatus(LeaveStatus.REJECTED);
        request.setRejectionReason("Cancelled by employee.");
        
        LeaveRequest saved = leaveRequestRepository.save(request);
        
        Notification notification = new Notification();
        notification.setEmployee(request.getEmployee());
        notification.setTitle("Leave Request Cancelled");
        notification.setMessage("You have cancelled your leave request for " + request.getLeaveType().name() + ".");
        notification.setType("INFO");
        notificationRepository.save(notification);
        
        return leaveRequestMapper.toDto(saved);
    }
}
