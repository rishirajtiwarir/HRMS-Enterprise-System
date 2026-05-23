package com.enterprise.hrms.mapper;

import com.enterprise.hrms.dto.DepartmentDto;
import com.enterprise.hrms.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public DepartmentDto toDto(Department department) {
        if (department == null) {
            return null;
        }
        DepartmentDto dto = new DepartmentDto();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setCode(department.getCode());
        dto.setDescription(department.getDescription());
        if (department.getEmployees() != null) {
            dto.setEmployeeCount(department.getEmployees().size());
        } else {
            dto.setEmployeeCount(0);
        }
        return dto;
    }

    public Department toEntity(DepartmentDto dto) {
        if (dto == null) {
            return null;
        }
        Department department = new Department();
        department.setId(dto.getId());
        department.setName(dto.getName());
        department.setCode(dto.getCode());
        department.setDescription(dto.getDescription());
        return department;
    }
}
