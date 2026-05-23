package com.enterprise.hrms.controller;

import com.enterprise.hrms.dto.AttendanceDto;
import com.enterprise.hrms.exception.BadRequestException;
import com.enterprise.hrms.security.UserPrincipal;
import com.enterprise.hrms.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller managing daily check-in and check-out and attendance reports.
 */
@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    // Check-in for current day (Authenticated Employee)
    @PostMapping("/check-in")
    public ResponseEntity<AttendanceDto> checkIn(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser.getEmployeeId() == null) {
            throw new com.enterprise.hrms.exception.BadRequestException("Authenticated user has no employee profile associated to mark attendance.");
        }
        AttendanceDto attendance = attendanceService.checkIn(currentUser.getEmployeeId());
        return ResponseEntity.ok(attendance);
    }

    // Check-out for current day (Authenticated Employee)
    @PostMapping("/check-out")
    public ResponseEntity<AttendanceDto> checkOut(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser.getEmployeeId() == null) {
            throw new com.enterprise.hrms.exception.BadRequestException("Authenticated user has no employee profile associated to mark attendance.");
        }
        AttendanceDto attendance = attendanceService.checkOut(currentUser.getEmployeeId());
        return ResponseEntity.ok(attendance);
    }

    // Get today's attendance status of current logged-in employee
    @GetMapping("/today-status")
    public ResponseEntity<AttendanceDto> getTodayStatus(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser.getEmployeeId() == null) {
            return ResponseEntity.ok(null);
        }
        AttendanceDto status = attendanceService.getTodayStatus(currentUser.getEmployeeId());
        return ResponseEntity.ok(status);
    }

    // Retrieve attendance history of a specific employee (ADMIN, HR, or own record)
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or #employeeId == authentication.principal.employeeId")
    public ResponseEntity<List<AttendanceDto>> getEmployeeAttendanceHistory(
            @PathVariable Long employeeId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<AttendanceDto> history = attendanceService.getEmployeeAttendanceHistory(employeeId, startDate, endDate);
        return ResponseEntity.ok(history);
    }

    // Retrieve all of today's logs (ADMIN or HR)
    @GetMapping("/today")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<List<AttendanceDto>> getAllTodayAttendance() {
        List<AttendanceDto> todayAttendance = attendanceService.getAllTodayAttendance();
        return ResponseEntity.ok(todayAttendance);
    }
}
