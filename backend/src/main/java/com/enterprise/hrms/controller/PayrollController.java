package com.enterprise.hrms.controller;

import com.enterprise.hrms.dto.PayrollDto;
import com.enterprise.hrms.service.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.enterprise.hrms.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller managing employee payroll generation, histories, and payslip downloads.
 */
@RestController
@RequestMapping("/payroll")
public class PayrollController {

    @Autowired
    private PayrollService payrollService;

    // Generate payroll for a single employee (ADMIN or HR only)
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<PayrollDto> generateMonthlyPayroll(
            @RequestParam("employeeId") Long employeeId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "allowances", defaultValue = "0.0") Double allowances,
            @RequestParam(value = "deductions", defaultValue = "0.0") Double deductions) {
        
        PayrollDto payroll = payrollService.generateMonthlyPayroll(employeeId, startDate, endDate, allowances, deductions);
        return ResponseEntity.ok(payroll);
    }

    // Generate bulk payroll for all active employees for a period (ADMIN or HR only)
    @PostMapping("/generate-bulk")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<List<PayrollDto>> generateBulkPayroll(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<PayrollDto> payrolls = payrollService.generateBulkPayroll(startDate, endDate);
        return ResponseEntity.ok(payrolls);
    }

    // Retrieve payroll history of a specific employee (ADMIN, HR, or self)
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or #employeeId == authentication.principal.employeeId")
    public ResponseEntity<List<PayrollDto>> getEmployeePayrollHistory(@PathVariable Long employeeId) {
        List<PayrollDto> history = payrollService.getEmployeePayrollHistory(employeeId);
        return ResponseEntity.ok(history);
    }

    // Retrieve all payroll runs (ADMIN or HR only)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<List<PayrollDto>> getAllPayrolls() {
        List<PayrollDto> payrolls = payrollService.getAllPayrolls();
        return ResponseEntity.ok(payrolls);
    }

    // Update payout status of payroll: e.g. mark as PAID (ADMIN or HR only)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<PayrollDto> updatePayrollStatus(
            @PathVariable Long id,
            @RequestParam("status") String status) {
        
        PayrollDto updatedPayroll = payrollService.updatePayrollStatus(id, status);
        return ResponseEntity.ok(updatedPayroll);
    }

    // Download formatted Payslip text/PDF document (ADMIN, HR, or self)
    @GetMapping("/{id}/payslip")
    public ResponseEntity<byte[]> downloadPayslip(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        boolean isAdminOrHr = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));
        
        if (!isAdminOrHr) {
            PayrollDto record = payrollService.getAllPayrolls().stream()
                    .filter(p -> p.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new com.enterprise.hrms.exception.ResourceNotFoundException("Payroll record not found with ID: " + id));
            
            if (currentUser.getEmployeeId() == null || !currentUser.getEmployeeId().equals(record.getEmployeeId())) {
                throw new com.enterprise.hrms.exception.BadRequestException("Access denied: You can only download your own payslip.");
            }
        }

        byte[] pdfBytes = payrollService.generatePayslipPdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payslip_" + id + ".txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(pdfBytes);
    }
}
