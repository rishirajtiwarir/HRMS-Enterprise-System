package com.enterprise.hrms.service;

import com.enterprise.hrms.dto.DashboardStatsDto;
import com.enterprise.hrms.dto.EmployeeDto;
import org.springframework.data.domain.Page;

public interface EmployeeService {
    Page<EmployeeDto> getAllEmployees(String search, int page, int size, String sortBy, String sortDir);
    EmployeeDto getEmployeeById(Long id);
    EmployeeDto createEmployee(EmployeeDto employeeDto);
    EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto);
    void deleteEmployee(Long id);
    EmployeeDto updateProfileImage(Long id, String imagePath);
    DashboardStatsDto getDashboardStats();
}
