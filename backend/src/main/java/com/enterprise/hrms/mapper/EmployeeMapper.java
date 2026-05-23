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
        dto.setSalary(employee.getSalary());

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

        return employee;
    }
}
