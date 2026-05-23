package com.enterprise.hrms.service;

import com.enterprise.hrms.dto.DepartmentDto;
import java.util.List;

public interface DepartmentService {
    List<DepartmentDto> getAllDepartments();
    DepartmentDto getDepartmentById(Long id);
    DepartmentDto createDepartment(DepartmentDto departmentDto);
    DepartmentDto updateDepartment(Long id, DepartmentDto departmentDto);
    void deleteDepartment(Long id);
}
