package com.enterprise.hrms.service;

import com.enterprise.hrms.dto.PayrollDto;
import java.time.LocalDate;
import java.util.List;

public interface PayrollService {
    PayrollDto generateMonthlyPayroll(Long employeeId, LocalDate start, LocalDate end, Double allowances, Double deductions);
    List<PayrollDto> generateBulkPayroll(LocalDate start, LocalDate end);
    List<PayrollDto> getEmployeePayrollHistory(Long employeeId);
    List<PayrollDto> getAllPayrolls();
    PayrollDto updatePayrollStatus(Long id, String status);
    byte[] generatePayslipPdf(Long payrollId);
}
