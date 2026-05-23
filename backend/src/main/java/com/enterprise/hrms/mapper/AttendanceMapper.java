package com.enterprise.hrms.mapper;

import com.enterprise.hrms.dto.AttendanceDto;
import com.enterprise.hrms.entity.Attendance;
import com.enterprise.hrms.entity.AttendanceStatus;
import org.springframework.stereotype.Component;

@Component
public class AttendanceMapper {

    public AttendanceDto toDto(Attendance attendance) {
        if (attendance == null) {
            return null;
        }

        AttendanceDto dto = new AttendanceDto();
        dto.setId(attendance.getId());
        dto.setDate(attendance.getDate());
        dto.setCheckIn(attendance.getCheckIn());
        dto.setCheckOut(attendance.getCheckOut());
        dto.setStatus(attendance.getStatus().name());
        dto.setWorkHours(attendance.getWorkHours());

        if (attendance.getEmployee() != null) {
            dto.setEmployeeId(attendance.getEmployee().getId());
            dto.setEmployeeName(attendance.getEmployee().getFirstName() + " " + attendance.getEmployee().getLastName());
        }

        return dto;
    }

    public Attendance toEntity(AttendanceDto dto) {
        if (dto == null) {
            return null;
        }

        Attendance attendance = new Attendance();
        attendance.setId(dto.getId());
        attendance.setDate(dto.getDate());
        attendance.setCheckIn(dto.getCheckIn());
        attendance.setCheckOut(dto.getCheckOut());
        if (dto.getStatus() != null) {
            attendance.setStatus(AttendanceStatus.valueOf(dto.getStatus()));
        }
        attendance.setWorkHours(dto.getWorkHours());

        return attendance;
    }
}
