package com.enterprise.hrms.service;

import com.enterprise.hrms.dto.AttendanceDto;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    AttendanceDto checkIn(Long employeeId);
    AttendanceDto checkOut(Long employeeId);
    AttendanceDto getTodayStatus(Long employeeId);
    List<AttendanceDto> getEmployeeAttendanceHistory(Long employeeId, LocalDate startDate, LocalDate endDate);
    List<AttendanceDto> getAllTodayAttendance();
}
