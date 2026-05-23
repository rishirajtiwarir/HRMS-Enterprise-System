package com.enterprise.hrms.service.impl;

import com.enterprise.hrms.dto.PayrollDto;
import com.enterprise.hrms.entity.*;
import com.enterprise.hrms.exception.BadRequestException;
import com.enterprise.hrms.exception.ResourceNotFoundException;
import com.enterprise.hrms.mapper.PayrollMapper;
import com.enterprise.hrms.repository.EmployeeRepository;
import com.enterprise.hrms.repository.PayrollRepository;
import com.enterprise.hrms.repository.NotificationRepository;
import com.enterprise.hrms.service.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PayrollServiceImpl implements PayrollService {

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PayrollMapper payrollMapper;

    @Override
    @Transactional
    public PayrollDto generateMonthlyPayroll(Long employeeId, LocalDate start, LocalDate end, Double allowances, Double deductions) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        // Check if payroll already run for this cycle
        Optional<Payroll> existing = payrollRepository.findByEmployeeIdAndPayPeriodStartAndPayPeriodEnd(employeeId, start, end);
        if (existing.isPresent()) {
            throw new BadRequestException("Payroll has already been generated for this employee for the selected period.");
        }

        Payroll payroll = new Payroll();
        payroll.setEmployee(employee);
        payroll.setPayPeriodStart(start);
        payroll.setPayPeriodEnd(end);
        payroll.setBasicSalary(employee.getSalary());
        payroll.setAllowances(allowances);
        payroll.setDeductions(deductions);
        payroll.setStatus(PayrollStatus.PENDING);

        Payroll savedPayroll = payrollRepository.save(payroll);
        return payrollMapper.toDto(savedPayroll);
    }

    @Override
    @Transactional
    public List<PayrollDto> generateBulkPayroll(LocalDate start, LocalDate end) {
        List<Employee> activeEmployees = employeeRepository.findAll().stream()
                .filter(emp -> emp.getStatus() == EmployeeStatus.ACTIVE)
                .collect(Collectors.toList());

        List<Payroll> generatedPayrolls = new ArrayList<>();

        for (Employee emp : activeEmployees) {
            Optional<Payroll> existing = payrollRepository.findByEmployeeIdAndPayPeriodStartAndPayPeriodEnd(emp.getId(), start, end);
            if (existing.isEmpty()) {
                Payroll payroll = new Payroll();
                payroll.setEmployee(emp);
                payroll.setPayPeriodStart(start);
                payroll.setPayPeriodEnd(end);
                payroll.setBasicSalary(emp.getSalary());
                
                // Set default mock allowances & deductions (e.g. 10% allowance, 5% deduction)
                payroll.setAllowances(emp.getSalary() * 0.10);
                payroll.setDeductions(emp.getSalary() * 0.05);
                payroll.setStatus(PayrollStatus.PENDING);
                
                generatedPayrolls.add(payrollRepository.save(payroll));
            }
        }

        return generatedPayrolls.stream()
                .map(payrollMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollDto> getEmployeePayrollHistory(Long employeeId) {
        return payrollRepository.findByEmployeeId(employeeId).stream()
                .map(payrollMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollDto> getAllPayrolls() {
        return payrollRepository.findAll().stream()
                .map(payrollMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PayrollDto updatePayrollStatus(Long id, String status) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found with ID: " + id));

        PayrollStatus newStatus = PayrollStatus.valueOf(status);
        payroll.setStatus(newStatus);
        
        if (newStatus == PayrollStatus.PAID) {
            payroll.setPaymentDate(LocalDate.now());

            // Notify Employee
            Notification notification = new Notification();
            notification.setEmployee(payroll.getEmployee());
            notification.setTitle("Salary Credited");
            notification.setMessage("Your net salary of $" + payroll.getNetSalary() + " for the period " 
                    + payroll.getPayPeriodStart() + " to " + payroll.getPayPeriodEnd() + " has been processed.");
            notification.setType("SUCCESS");
            notificationRepository.save(notification);
        }

        Payroll updatedPayroll = payrollRepository.save(payroll);
        return payrollMapper.toDto(updatedPayroll);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePayslipPdf(Long payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found with ID: " + payrollId));

        Employee emp = payroll.getEmployee();

        // Formats a clean, text-based payslip representation (e.g. ASCII invoice style)
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================\n");
        sb.append("                       ENTERPRISE HRMS - PAYSLIP                        \n");
        sb.append("========================================================================\n\n");
        sb.append(String.format("Payslip ID   : %d\n", payroll.getId()));
        sb.append(String.format("Pay Period   : %s to %s\n", payroll.getPayPeriodStart(), payroll.getPayPeriodEnd()));
        sb.append(String.format("Payment Date : %s\n", payroll.getPaymentDate() != null ? payroll.getPaymentDate() : "N/A"));
        sb.append(String.format("Status       : %s\n\n", payroll.getStatus()));
        sb.append("EMPLOYEE DETAILS:\n");
        sb.append("-----------------\n");
        sb.append(String.format("Employee Name : %s %s\n", emp.getFirstName(), emp.getLastName()));
        sb.append(String.format("Designation   : %s\n", emp.getDesignation()));
        sb.append(String.format("Email Address : %s\n\n", emp.getEmail()));
        sb.append("EARNINGS & DEDUCTIONS:\n");
        sb.append("----------------------\n");
        sb.append(String.format("Basic Salary  : $ %10.2f\n", payroll.getBasicSalary()));
        sb.append(String.format("Allowances    : $ %10.2f\n", payroll.getAllowances()));
        sb.append(String.format("Deductions    : $ %10.2f\n", payroll.getDeductions()));
        sb.append("--------------------------------------\n");
        sb.append(String.format("NET SALARY    : $ %10.2f\n\n", payroll.getNetSalary()));
        sb.append("========================================================================\n");
        sb.append("        This is a computer-generated document, no signature required.   \n");
        sb.append("========================================================================\n");

        return sb.toString().getBytes();
    }
}
