package com.enterprise.hrms.service.impl;

import com.enterprise.hrms.dto.AttendanceDto;
import com.enterprise.hrms.entity.Attendance;
import com.enterprise.hrms.entity.AttendanceStatus;
import com.enterprise.hrms.entity.Employee;
import com.enterprise.hrms.exception.BadRequestException;
import com.enterprise.hrms.exception.ResourceNotFoundException;
import com.enterprise.hrms.mapper.AttendanceMapper;
import com.enterprise.hrms.repository.AttendanceRepository;
import com.enterprise.hrms.repository.EmployeeRepository;
import com.enterprise.hrms.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceMapper attendanceMapper;

    // Standard business check-in start boundary (e.g. 09:30 AM is the cutoff for late status)
    private static final LocalTime LATE_THRESHOLD = LocalTime.of(9, 30);

    @Override
    @Transactional
    public AttendanceDto checkIn(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        LocalDate today = LocalDate.now();
        Optional<Attendance> existingAttendance = attendanceRepository.findByEmployeeIdAndDate(employeeId, today);
        if (existingAttendance.isPresent()) {
            throw new BadRequestException("Attendance already marked for today!");
        }

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setDate(today);
        
        LocalTime now = LocalTime.now();
        attendance.setCheckIn(now);

        // Calculate status
        if (now.isAfter(LATE_THRESHOLD)) {
            attendance.setStatus(AttendanceStatus.LATE);
        } else {
            attendance.setStatus(AttendanceStatus.PRESENT);
        }

        Attendance savedAttendance = attendanceRepository.save(attendance);
        return attendanceMapper.toDto(savedAttendance);
    }

    @Override
    @Transactional
    public AttendanceDto checkOut(Long employeeId) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new BadRequestException("No check-in record found for today. Please check-in first."));

        if (attendance.getCheckOut() != null) {
            throw new BadRequestException("Already checked out for today!");
        }

        LocalTime now = LocalTime.now();
        attendance.setCheckOut(now);

        // Calculate hours worked
        Duration duration = Duration.between(attendance.getCheckIn(), now);
        double hours = duration.toMinutes() / 60.0;
        
        // Round to 2 decimal places
        double roundedHours = Math.round(hours * 100.0) / 100.0;
        attendance.setWorkHours(roundedHours);

        Attendance updatedAttendance = attendanceRepository.save(attendance);
        return attendanceMapper.toDto(updatedAttendance);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceDto getTodayStatus(Long employeeId) {
        LocalDate today = LocalDate.now();
        return attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
                .map(attendanceMapper::toDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceDto> getEmployeeAttendanceHistory(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, startDate, endDate).stream()
                .map(attendanceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceDto> getAllTodayAttendance() {
        return attendanceRepository.findByDate(LocalDate.now()).stream()
                .map(attendanceMapper::toDto)
                .collect(Collectors.toList());
    }
}
