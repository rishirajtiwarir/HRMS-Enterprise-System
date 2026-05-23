package com.enterprise.hrms.mapper;

import com.enterprise.hrms.dto.PayrollDto;
import com.enterprise.hrms.entity.Payroll;
import com.enterprise.hrms.entity.PayrollStatus;
import org.springframework.stereotype.Component;

@Component
public class PayrollMapper {

    public PayrollDto toDto(Payroll payroll) {
        if (payroll == null) {
            return null;
        }

        PayrollDto dto = new PayrollDto();
        dto.setId(payroll.getId());
        dto.setPayPeriodStart(payroll.getPayPeriodStart());
        dto.setPayPeriodEnd(payroll.getPayPeriodEnd());
        dto.setBasicSalary(payroll.getBasicSalary());
        dto.setAllowances(payroll.getAllowances());
        dto.setDeductions(payroll.getDeductions());
        dto.setNetSalary(payroll.getNetSalary());
        dto.setStatus(payroll.getStatus().name());
        dto.setPaymentDate(payroll.getPaymentDate());
        dto.setPayslipPdfPath(payroll.getPayslipPdfPath());

        if (payroll.getEmployee() != null) {
            dto.setEmployeeId(payroll.getEmployee().getId());
            dto.setEmployeeName(payroll.getEmployee().getFirstName() + " " + payroll.getEmployee().getLastName());
            dto.setDesignation(payroll.getEmployee().getDesignation());
        }

        return dto;
    }

    public Payroll toEntity(PayrollDto dto) {
        if (dto == null) {
            return null;
        }

        Payroll payroll = new Payroll();
        payroll.setId(dto.getId());
        payroll.setPayPeriodStart(dto.getPayPeriodStart());
        payroll.setPayPeriodEnd(dto.getPayPeriodEnd());
        payroll.setBasicSalary(dto.getBasicSalary());
        payroll.setAllowances(dto.getAllowances());
        payroll.setDeductions(dto.getDeductions());
        payroll.setNetSalary(dto.getNetSalary());
        if (dto.getStatus() != null) {
            payroll.setStatus(PayrollStatus.valueOf(dto.getStatus()));
        }
        payroll.setPaymentDate(dto.getPaymentDate());
        payroll.setPayslipPdfPath(dto.getPayslipPdfPath());

        return payroll;
    }
}
