package com.enterprise.hrms.service;

import com.enterprise.hrms.dto.LeaveRequestDto;
import com.enterprise.hrms.entity.LeaveStatus;

import java.util.List;
import java.util.Map;

public interface LeaveRequestService {
    LeaveRequestDto applyLeave(LeaveRequestDto leaveRequestDto);
    LeaveRequestDto resolveLeave(Long id, LeaveStatus status, String rejectionReason);
    List<LeaveRequestDto> getEmployeeLeaveHistory(Long employeeId);
    List<LeaveRequestDto> getAllLeaveRequests();
    Map<String, Integer> getLeaveBalances(Long employeeId);
}
