package com.enterprise.hrms.mapper;

import com.enterprise.hrms.dto.EmployeeDto;
import com.enterprise.hrms.entity.Employee;
import com.enterprise.hrms.entity.EmployeeStatus;
import com.enterprise.hrms.entity.Role;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class EmployeeMapper {

    public EmployeeDto toDto(Employee employee) {
        if (employee == null) {
            return null;
        }

        EmployeeDto dto = new EmployeeDto();
        dto.setId(employee.getId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setPhone(employee.getPhone());
        dto.setDateOfBirth(employee.getDateOfBirth());
        dto.setJoiningDate(employee.getJoiningDate());
        dto.setDesignation(employee.getDesignation());
        dto.setStatus(employee.getStatus().name());
        dto.setProfileImage(employee.getProfileImage());

        // Enforce salary privacy
        try {
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
                Object principal = auth.getPrincipal();
                if (principal instanceof com.enterprise.hrms.security.UserPrincipal) {
                    com.enterprise.hrms.security.UserPrincipal currentUser = (com.enterprise.hrms.security.UserPrincipal) principal;
                    boolean isSelf = currentUser.getEmployeeId() != null && currentUser.getEmployeeId().equals(employee.getId());
                    boolean hasPrivilegedRole = auth.getAuthorities().stream()
                            .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                            .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_SUPER_ADMIN") || role.equals("ROLE_HR") || role.equals("ROLE_HR_MANAGER"));
                    
                    if (isSelf || hasPrivilegedRole) {
                        dto.setSalary(employee.getSalary());
                    } else {
                        dto.setSalary(null);
                    }
                } else {
                    dto.setSalary(employee.getSalary());
                }
            } else {
                dto.setSalary(employee.getSalary());
            }
        } catch (Exception e) {
            dto.setSalary(employee.getSalary());
        }

        dto.setEmergencyContactName(employee.getEmergencyContactName());
        dto.setEmergencyContactPhone(employee.getEmergencyContactPhone());
        dto.setEmergencyContactRelation(employee.getEmergencyContactRelation());

        if (employee.getManager() != null) {
            dto.setManagerId(employee.getManager().getId());
            dto.setManagerName(employee.getManager().getFirstName() + " " + employee.getManager().getLastName());
        }

        if (employee.getDepartment() != null) {
            dto.setDepartmentId(employee.getDepartment().getId());
            dto.setDepartmentName(employee.getDepartment().getName());
        }

        if (employee.getUser() != null) {
            dto.setUsername(employee.getUser().getUsername());
            if (employee.getUser().getRoles() != null && !employee.getUser().getRoles().isEmpty()) {
                // Return roles as a comma-separated string or first role
                dto.setUserRole(employee.getUser().getRoles().stream()
                        .map(Role::getName)
                        .map(Enum::name)
                        .collect(Collectors.joining(",")));
            }
        }

        return dto;
    }

    public Employee toEntity(EmployeeDto dto) {
        if (dto == null) {
            return null;
        }

        Employee employee = new Employee();
        employee.setId(dto.getId());
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setDateOfBirth(dto.getDateOfBirth());
        employee.setJoiningDate(dto.getJoiningDate());
        employee.setDesignation(dto.getDesignation());
        if (dto.getStatus() != null) {
            employee.setStatus(EmployeeStatus.valueOf(dto.getStatus()));
        }
        employee.setProfileImage(dto.getProfileImage());
        employee.setSalary(dto.getSalary());

        employee.setEmergencyContactName(dto.getEmergencyContactName());
        employee.setEmergencyContactPhone(dto.getEmergencyContactPhone());
        employee.setEmergencyContactRelation(dto.getEmergencyContactRelation());

        if (dto.getManagerId() != null) {
            Employee manager = new Employee();
            manager.setId(dto.getManagerId());
            employee.setManager(manager);
        }

        return employee;
    }
}
