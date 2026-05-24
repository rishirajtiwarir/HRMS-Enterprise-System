package com.enterprise.hrms.controller;

import com.enterprise.hrms.entity.*;
import com.enterprise.hrms.repository.*;
import com.enterprise.hrms.dto.*;
import com.enterprise.hrms.exception.ResourceNotFoundException;
import com.enterprise.hrms.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.enterprise.hrms.security.UserPrincipal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/operations")
public class AdvancedOperationsController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ResignationRepository resignationRepository;

    @Autowired
    private EmployeePromotionRepository employeePromotionRepository;

    @Autowired
    private DepartmentTransferRepository departmentTransferRepository;

    @Autowired
    private AttendanceCorrectionRepository attendanceCorrectionRepository;

    @Autowired
    private SalaryRevisionRepository salaryRevisionRepository;

    @Autowired
    private EmployeeDocumentRepository employeeDocumentRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    // Helper to log audit actions
    private void audit(String action, String details, String performedBy) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setDetails(details);
        log.setPerformedBy(performedBy);
        auditLogRepository.save(log);
    }

    // 1. Promote Employee (Admin or HR)
    @PostMapping("/employees/{id}/promote")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> promoteEmployee(
            @PathVariable Long id,
            @RequestParam("newDesignation") String newDesignation,
            @RequestParam("newSalary") Double newSalary,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        // Record Salary Revision history
        SalaryRevision revision = new SalaryRevision();
        revision.setEmployee(employee);
        revision.setPreviousSalary(employee.getSalary());
        revision.setNewSalary(newSalary);
        revision.setRevisionDate(LocalDate.now());
        revision.setRevisedBy(currentUser.getUsername());
        salaryRevisionRepository.save(revision);

        // Record Promotion history
        EmployeePromotion promotion = new EmployeePromotion();
        promotion.setEmployee(employee);
        promotion.setPreviousDesignation(employee.getDesignation());
        promotion.setNewDesignation(newDesignation);
        promotion.setPreviousSalary(employee.getSalary());
        promotion.setNewSalary(newSalary);
        promotion.setPromotionDate(LocalDate.now());
        employeePromotionRepository.save(promotion);

        // Update employee record
        employee.setDesignation(newDesignation);
        employee.setSalary(newSalary);
        employeeRepository.save(employee);

        audit("PROMOTION", "Promoted employee " + employee.getEmail() + " to " + newDesignation + " with salary " + newSalary, currentUser.getUsername());

        return ResponseEntity.ok(new MessageResponse("Employee promoted successfully."));
    }

    // 2. Transfer Department (Admin or HR)
    @PostMapping("/employees/{id}/transfer")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> transferEmployee(
            @PathVariable Long id,
            @RequestParam("newDepartmentId") Long newDepartmentId,
            @RequestParam(value = "reason", defaultValue = "") String reason,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Department newDept = departmentRepository.findById(newDepartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        // Record Department Transfer history
        DepartmentTransfer transfer = new DepartmentTransfer();
        transfer.setEmployee(employee);
        transfer.setPreviousDepartment(employee.getDepartment());
        transfer.setNewDepartment(newDept);
        transfer.setTransferDate(LocalDate.now());
        transfer.setReason(reason);
        departmentTransferRepository.save(transfer);

        // Update employee department
        employee.setDepartment(newDept);
        employeeRepository.save(employee);

        audit("TRANSFER", "Transferred employee " + employee.getEmail() + " to department " + newDept.getName(), currentUser.getUsername());

        return ResponseEntity.ok(new MessageResponse("Employee department transferred successfully."));
    }

    // 3. Submit Resignation (Self profile)
    @PostMapping("/employees/resign")
    public ResponseEntity<?> submitResignation(
            @RequestParam("reason") String reason,
            @RequestParam("lastWorkingDate") String lastWorkingDate,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if (currentUser.getEmployeeId() == null) {
            throw new BadRequestException("No employee profile found for user.");
        }

        Employee employee = employeeRepository.findById(currentUser.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Resignation resignation = new Resignation();
        resignation.setEmployee(employee);
        resignation.setReason(reason);
        resignation.setSubmissionDate(LocalDate.now());
        resignation.setLastWorkingDate(LocalDate.parse(lastWorkingDate));
        resignation.setStatus("PENDING");
        resignationRepository.save(resignation);

        audit("RESIGNATION_SUBMISSION", "Employee " + employee.getEmail() + " submitted resignation for " + lastWorkingDate, currentUser.getUsername());

        return ResponseEntity.ok(new MessageResponse("Resignation submitted successfully."));
    }

    // 4. Handle Resignation Approvals (Admin or HR)
    @PutMapping("/resignations/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> approveResignation(
            @PathVariable Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "feedback", defaultValue = "") String feedback,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Resignation resignation = resignationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resignation record not found"));

        resignation.setStatus(status);
        resignation.setFeedback(feedback);
        resignationRepository.save(resignation);

        if ("APPROVED".equalsIgnoreCase(status)) {
            Employee employee = resignation.getEmployee();
            employee.setStatus(EmployeeStatus.TERMINATED);
            employeeRepository.save(employee);
            audit("RESIGNATION_APPROVED", "Approved resignation for employee " + employee.getEmail() + ". Status updated to TERMINATED.", currentUser.getUsername());
        } else {
            audit("RESIGNATION_REJECTED", "Rejected resignation for employee " + resignation.getEmployee().getEmail(), currentUser.getUsername());
        }

        return ResponseEntity.ok(new MessageResponse("Resignation status updated to " + status));
    }

    // 5. Retrieve all resignations (Admin or HR)
    @GetMapping("/resignations")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> getAllResignations() {
        return ResponseEntity.ok(resignationRepository.findAll());
    }

    // 6. Upload Employee Document (Self or Admin/HR)
    @PostMapping("/employees/{employeeId}/documents")
    public ResponseEntity<?> uploadDocument(
            @PathVariable Long employeeId,
            @RequestParam("documentName") String documentName,
            @RequestParam("documentType") String documentType,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        boolean isSelf = currentUser.getEmployeeId() != null && currentUser.getEmployeeId().equals(employeeId);
        boolean isAdminOrHr = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));

        if (!isSelf && !isAdminOrHr) {
            throw new BadRequestException("Access denied.");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        String base64Content = payload.get("fileContent");
        if (base64Content == null || base64Content.isEmpty()) {
            throw new BadRequestException("File content must not be empty.");
        }

        EmployeeDocument doc = new EmployeeDocument();
        doc.setEmployee(employee);
        doc.setDocumentName(documentName);
        doc.setDocumentType(documentType);
        doc.setFileContent(base64Content);
        employeeDocumentRepository.save(doc);

        audit("DOCUMENT_UPLOAD", "Uploaded document " + documentName + " for employee " + employee.getEmail(), currentUser.getUsername());

        return ResponseEntity.ok(new MessageResponse("Document uploaded successfully."));
    }

    // 7. Get Employee Documents (Self or Admin/HR)
    @GetMapping("/employees/{employeeId}/documents")
    public ResponseEntity<?> getEmployeeDocuments(
            @PathVariable Long employeeId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        boolean isSelf = currentUser.getEmployeeId() != null && currentUser.getEmployeeId().equals(employeeId);
        boolean isAdminOrHr = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));

        if (!isSelf && !isAdminOrHr) {
            throw new BadRequestException("Access denied.");
        }

        return ResponseEntity.ok(employeeDocumentRepository.findByEmployeeIdOrderByUploadedAtDesc(employeeId));
    }

    // 8. Attendance Correction Submission (Self)
    @PostMapping("/attendance/correction")
    public ResponseEntity<?> requestCorrection(
            @RequestParam(value = "attendanceId", required = false) Long attendanceId,
            @RequestParam("date") String date,
            @RequestParam("requestedCheckIn") String requestedCheckIn,
            @RequestParam("requestedCheckOut") String requestedCheckOut,
            @RequestParam("reason") String reason,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if (currentUser.getEmployeeId() == null) {
            throw new BadRequestException("No employee profile found.");
        }

        Employee employee = employeeRepository.findById(currentUser.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        AttendanceCorrection correction = new AttendanceCorrection();
        if (attendanceId != null) {
            Attendance attendance = attendanceRepository.findById(attendanceId).orElse(null);
            correction.setAttendance(attendance);
        }
        correction.setEmployee(employee);
        correction.setDate(LocalDate.parse(date));
        correction.setRequestedCheckIn(LocalTime.parse(requestedCheckIn));
        correction.setRequestedCheckOut(LocalTime.parse(requestedCheckOut));
        correction.setReason(reason);
        correction.setStatus("PENDING");
        attendanceCorrectionRepository.save(correction);

        audit("ATTENDANCE_CORRECTION_REQUEST", "Correction requested by " + employee.getEmail() + " for date " + date, currentUser.getUsername());

        return ResponseEntity.ok(new MessageResponse("Attendance correction requested."));
    }

    // 9. Approve/Reject Attendance Correction (Manager, Admin, or HR)
    @PutMapping("/attendance/correction/{id}")
    public ResponseEntity<?> handleCorrection(
            @PathVariable Long id,
            @RequestParam("status") String status,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        AttendanceCorrection correction = attendanceCorrectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Correction request not found"));

        // Verify authorization (Must be direct manager, admin, or HR)
        boolean isAdminOrHr = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));
        boolean isManager = correction.getEmployee().getManager() != null &&
                currentUser.getEmployeeId() != null &&
                correction.getEmployee().getManager().getId().equals(currentUser.getEmployeeId());

        if (!isAdminOrHr && !isManager) {
            throw new BadRequestException("Access denied: You are not authorized to approve this correction.");
        }

        correction.setStatus(status);
        attendanceCorrectionRepository.save(correction);

        if ("APPROVED".equalsIgnoreCase(status)) {
            Attendance att = correction.getAttendance();
            if (att == null) {
                att = new Attendance();
                att.setEmployee(correction.getEmployee());
                att.setDate(correction.getDate());
            }
            att.setCheckIn(correction.getRequestedCheckIn());
            att.setCheckOut(correction.getRequestedCheckOut());
            att.setStatus(AttendanceStatus.PRESENT);
            
            // Calculate work hours
            double checkInHours = att.getCheckIn().getHour() + (att.getCheckIn().getMinute() / 60.0);
            double checkOutHours = att.getCheckOut().getHour() + (att.getCheckOut().getMinute() / 60.0);
            double totalHours = checkOutHours - checkInHours;
            att.setWorkHours(totalHours > 0 ? totalHours : 0.0);
            
            // Late check-in
            if (att.getCheckIn().isAfter(LocalTime.of(9, 15))) {
                att.setIsLate(true);
            } else {
                att.setIsLate(false);
            }
            
            // Overtime
            if (totalHours > 8.0) {
                att.setOvertimeMinutes((int) ((totalHours - 8.0) * 60));
            } else {
                att.setOvertimeMinutes(0);
            }
            
            attendanceRepository.save(att);
            audit("ATTENDANCE_CORRECTION_APPROVED", "Approved check log for " + correction.getEmployee().getEmail() + " on " + correction.getDate(), currentUser.getUsername());
        } else {
            audit("ATTENDANCE_CORRECTION_REJECTED", "Rejected check log for " + correction.getEmployee().getEmail() + " on " + correction.getDate(), currentUser.getUsername());
        }

        return ResponseEntity.ok(new MessageResponse("Correction status updated to " + status));
    }

    // 10. Get Pending Team Corrections (Team Lead / Manager)
    @GetMapping("/attendance/corrections/pending")
    public ResponseEntity<?> getPendingCorrections(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser.getEmployeeId() == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(attendanceCorrectionRepository.findByEmployeeManagerIdAndStatus(currentUser.getEmployeeId(), "PENDING"));
    }

    // 11. Get Direct Reports Attendance (Team Lead / HR / Admin)
    @GetMapping("/attendance/team")
    public ResponseEntity<?> getTeamAttendance(
            @RequestParam("date") String dateStr,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        LocalDate date = LocalDate.parse(dateStr);
        boolean isAdminOrHr = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));

        List<Attendance> list;
        if (isAdminOrHr) {
            list = attendanceRepository.findByDate(date);
        } else {
            if (currentUser.getEmployeeId() == null) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            list = attendanceRepository.findByDate(date).stream()
                    .filter(a -> a.getEmployee().getManager() != null && a.getEmployee().getManager().getId().equals(currentUser.getEmployeeId()))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(list);
    }

    // 12. Corporate Holidays API
    @GetMapping("/holidays")
    public ResponseEntity<?> getHolidays() {
        return ResponseEntity.ok(holidayRepository.findAll());
    }

    @PostMapping("/holidays")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> createHoliday(
            @RequestParam("name") String name,
            @RequestParam("date") String dateStr,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        LocalDate date = LocalDate.parse(dateStr);
        if (holidayRepository.findByDate(date).isPresent()) {
            throw new BadRequestException("Holiday on this date already exists.");
        }

        Holiday hol = new Holiday();
        hol.setName(name);
        hol.setDate(date);
        holidayRepository.save(hol);

        audit("HOLIDAY_CREATED", "Created public holiday " + name + " for " + dateStr, currentUser.getUsername());

        return ResponseEntity.ok(hol);
    }

    @DeleteMapping("/holidays/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> deleteHoliday(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal currentUser) {
        Holiday hol = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found"));
        holidayRepository.delete(hol);

        audit("HOLIDAY_DELETED", "Deleted public holiday " + hol.getName(), currentUser.getUsername());

        return ResponseEntity.ok(new MessageResponse("Holiday deleted successfully."));
    }

    // 13. System Audit Trail Dashboard (Admin or HR)
    @GetMapping("/admin/audit-logs")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> getAuditLogs() {
        return ResponseEntity.ok(auditLogRepository.findAllByOrderByTimestampDesc());
    }

    // 14. Dashboard Metric Aggregations for Chart.js (Admin or HR)
    @GetMapping("/admin/analytics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> getAdminAnalytics() {
        Map<String, Object> data = new HashMap<>();

        // Metric A: Active Employee Distribution by Department
        List<Department> departments = departmentRepository.findAll();
        List<String> departmentNames = departments.stream().map(Department::getName).collect(Collectors.toList());
        List<Long> departmentEmployeeCounts = departments.stream()
                .map(dept -> employeeRepository.countByDepartmentId(dept.getId()))
                .collect(Collectors.toList());
        
        Map<String, Object> deptDist = new HashMap<>();
        deptDist.put("labels", departmentNames);
        deptDist.put("data", departmentEmployeeCounts);
        data.put("departmentDistribution", deptDist);

        // Metric B: Average Work Hours
        List<Attendance> allAttendance = attendanceRepository.findAll();
        double avgHours = allAttendance.stream()
                .filter(a -> a.getWorkHours() != null)
                .mapToDouble(Attendance::getWorkHours)
                .average()
                .orElse(0.0);
        data.put("averageWorkHours", avgHours);

        // Metric C: Approved vs Rejected Leaves
        long approvedLeaves = leaveRequestRepository.findAll().stream()
                .filter(l -> l.getStatus() == LeaveStatus.APPROVED)
                .count();
        long rejectedLeaves = leaveRequestRepository.findAll().stream()
                .filter(l -> l.getStatus() == LeaveStatus.REJECTED)
                .count();
        long pendingLeaves = leaveRequestRepository.findAll().stream()
                .filter(l -> l.getStatus() == LeaveStatus.PENDING)
                .count();

        Map<String, Long> leavesStats = new HashMap<>();
        leavesStats.put("approved", approvedLeaves);
        leavesStats.put("rejected", rejectedLeaves);
        leavesStats.put("pending", pendingLeaves);
        data.put("leaveStats", leavesStats);

        // Metric D: Employee Totals
        long totalEmployees = employeeRepository.count();
        long activeEmployees = employeeRepository.countByStatus(EmployeeStatus.ACTIVE);
        data.put("totalEmployees", totalEmployees);
        data.put("activeEmployees", activeEmployees);

        return ResponseEntity.ok(data);
    }
}
