package com.enterprise.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Data Transfer Object representing payroll details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String designation;
    private LocalDate payPeriodStart;
    private LocalDate payPeriodEnd;
    private Double basicSalary;
    private Double allowances;
    private Double deductions;
    private Double taxDeduction;
    private Double bonus;
    private Double netSalary;
    private String status;
    private LocalDate paymentDate;
    private String payslipPdfPath;
}
