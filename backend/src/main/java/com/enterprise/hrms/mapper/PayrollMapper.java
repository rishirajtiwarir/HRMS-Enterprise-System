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
        // Enforce role checks on financial payroll mapping
        boolean isAuthorized = false;
        try {
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
                Object principal = auth.getPrincipal();
                if (principal instanceof com.enterprise.hrms.security.UserPrincipal) {
                    com.enterprise.hrms.security.UserPrincipal currentUser = (com.enterprise.hrms.security.UserPrincipal) principal;
                    boolean isSelf = payroll.getEmployee() != null && currentUser.getEmployeeId() != null && currentUser.getEmployeeId().equals(payroll.getEmployee().getId());
                    boolean hasPrivilegedRole = auth.getAuthorities().stream()
                            .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                            .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_SUPER_ADMIN") || role.equals("ROLE_HR") || role.equals("ROLE_HR_MANAGER"));
                    
                    if (isSelf || hasPrivilegedRole) {
                        isAuthorized = true;
                    }
                }
            } else {
                isAuthorized = true; // allow mapping on startup/seed
            }
        } catch (Exception e) {
            isAuthorized = true; // fallback
        }

        if (isAuthorized) {
            dto.setBasicSalary(payroll.getBasicSalary());
            dto.setAllowances(payroll.getAllowances());
            dto.setDeductions(payroll.getDeductions());
            dto.setTaxDeduction(payroll.getTaxDeduction());
            dto.setBonus(payroll.getBonus());
            dto.setNetSalary(payroll.getNetSalary());
        } else {
            dto.setBasicSalary(null);
            dto.setAllowances(null);
            dto.setDeductions(null);
            dto.setTaxDeduction(null);
            dto.setBonus(null);
            dto.setNetSalary(null);
        }

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
        payroll.setTaxDeduction(dto.getTaxDeduction());
        payroll.setBonus(dto.getBonus());
        payroll.setNetSalary(dto.getNetSalary());
        if (dto.getStatus() != null) {
            payroll.setStatus(PayrollStatus.valueOf(dto.getStatus()));
        }
        payroll.setPaymentDate(dto.getPaymentDate());
        payroll.setPayslipPdfPath(dto.getPayslipPdfPath());

        return payroll;
    }
}
