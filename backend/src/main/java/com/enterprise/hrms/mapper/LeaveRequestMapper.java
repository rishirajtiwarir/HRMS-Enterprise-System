package com.enterprise.hrms.mapper;

import com.enterprise.hrms.dto.LeaveRequestDto;
import com.enterprise.hrms.entity.LeaveRequest;
import com.enterprise.hrms.entity.LeaveStatus;
import com.enterprise.hrms.entity.LeaveType;
import org.springframework.stereotype.Component;

@Component
public class LeaveRequestMapper {

    public LeaveRequestDto toDto(LeaveRequest leaveRequest) {
        if (leaveRequest == null) {
            return null;
        }

        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setId(leaveRequest.getId());
        dto.setLeaveType(leaveRequest.getLeaveType().name());
        dto.setStartDate(leaveRequest.getStartDate());
        dto.setEndDate(leaveRequest.getEndDate());
        dto.setReason(leaveRequest.getReason());
        dto.setStatus(leaveRequest.getStatus().name());
        dto.setRejectionReason(leaveRequest.getRejectionReason());
        dto.setSickLeaveDocumentUrl(leaveRequest.getSickLeaveDocumentUrl());
        dto.setStage(leaveRequest.getStage());
        dto.setCurrentApproverRole(leaveRequest.getCurrentApproverRole());
        dto.setCancelled(leaveRequest.getCancelled());
        dto.setCreatedAt(leaveRequest.getCreatedAt());

        if (leaveRequest.getEmployee() != null) {
            dto.setEmployeeId(leaveRequest.getEmployee().getId());
            dto.setEmployeeName(leaveRequest.getEmployee().getFirstName() + " " + leaveRequest.getEmployee().getLastName());
        }

        return dto;
    }

    public LeaveRequest toEntity(LeaveRequestDto dto) {
        if (dto == null) {
            return null;
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setId(dto.getId());
        if (dto.getLeaveType() != null) {
            leaveRequest.setLeaveType(LeaveType.valueOf(dto.getLeaveType()));
        }
        leaveRequest.setStartDate(dto.getStartDate());
        leaveRequest.setEndDate(dto.getEndDate());
        leaveRequest.setReason(dto.getReason());
        if (dto.getStatus() != null) {
            leaveRequest.setStatus(LeaveStatus.valueOf(dto.getStatus()));
        }
        leaveRequest.setRejectionReason(dto.getRejectionReason());
        leaveRequest.setSickLeaveDocumentUrl(dto.getSickLeaveDocumentUrl());
        if (dto.getStage() != null) {
            leaveRequest.setStage(dto.getStage());
        }
        leaveRequest.setCurrentApproverRole(dto.getCurrentApproverRole());
        if (dto.getCancelled() != null) {
            leaveRequest.setCancelled(dto.getCancelled());
        }

        return leaveRequest;
    }
}
