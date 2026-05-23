package com.enterprise.hrms.service.impl;

import com.enterprise.hrms.dto.DashboardStatsDto;
import com.enterprise.hrms.dto.EmployeeDto;
import com.enterprise.hrms.entity.*;
import com.enterprise.hrms.exception.BadRequestException;
import com.enterprise.hrms.exception.ResourceNotFoundException;
import com.enterprise.hrms.mapper.EmployeeMapper;
import com.enterprise.hrms.repository.AttendanceRepository;
import com.enterprise.hrms.repository.DepartmentRepository;
import com.enterprise.hrms.repository.EmployeeRepository;
import com.enterprise.hrms.repository.LeaveRequestRepository;
import com.enterprise.hrms.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDto> getAllEmployees(String search, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Employee> employeesPage;

        if (search != null && !search.trim().isEmpty()) {
            employeesPage = employeeRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    search, search, search, pageable
            );
        } else {
            employeesPage = employeeRepository.findAll(pageable);
        }

        return employeesPage.map(employeeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));
        return employeeMapper.toDto(employee);
    }

    @Override
    @Transactional
    public EmployeeDto createEmployee(EmployeeDto employeeDto) {
        if (employeeRepository.findByEmail(employeeDto.getEmail()).isPresent()) {
            throw new BadRequestException("Employee email is already registered!");
        }

        Employee employee = employeeMapper.toEntity(employeeDto);

        if (employeeDto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(employeeDto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + employeeDto.getDepartmentId()));
            employee.setDepartment(department);
        }

        employee.setStatus(EmployeeStatus.ACTIVE);
        Employee savedEmployee = employeeRepository.save(employee);
        return employeeMapper.toDto(savedEmployee);
    }

    @Override
    @Transactional
    public EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        // Email validation (cannot steal someone else's email)
        employeeRepository.findByEmail(employeeDto.getEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BadRequestException("Email is already used by another employee!");
            }
        });

        employee.setFirstName(employeeDto.getFirstName());
        employee.setLastName(employeeDto.getLastName());
        employee.setEmail(employeeDto.getEmail());
        employee.setPhone(employeeDto.getPhone());
        employee.setDateOfBirth(employeeDto.getDateOfBirth());
        employee.setJoiningDate(employeeDto.getJoiningDate());
        employee.setDesignation(employeeDto.getDesignation());
        employee.setSalary(employeeDto.getSalary());

        if (employeeDto.getStatus() != null) {
            employee.setStatus(EmployeeStatus.valueOf(employeeDto.getStatus()));
            // Deactivate credentials if employee is terminated
            if (employee.getStatus() == EmployeeStatus.TERMINATED && employee.getUser() != null) {
                employee.getUser().setActive(false);
            }
        }

        if (employeeDto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(employeeDto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + employeeDto.getDepartmentId()));
            employee.setDepartment(department);
        } else {
            employee.setDepartment(null);
        }

        Employee updatedEmployee = employeeRepository.save(employee);
        return employeeMapper.toDto(updatedEmployee);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));
        
        // Use soft delete by deactivating employee and associated user credentials
        employee.setStatus(EmployeeStatus.TERMINATED);
        if (employee.getUser() != null) {
            employee.getUser().setActive(false);
        }
        employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public EmployeeDto updateProfileImage(Long id, String imagePath) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));
        employee.setProfileImage(imagePath);
        Employee updatedEmployee = employeeRepository.save(employee);
        return employeeMapper.toDto(updatedEmployee);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        stats.setTotalEmployees(employeeRepository.count());
        stats.setActiveEmployees(employeeRepository.countByStatus(EmployeeStatus.ACTIVE));
        stats.setTotalDepartments(departmentRepository.count());
        
        // Count present today
        long presentToday = attendanceRepository.findByDate(LocalDate.now()).stream()
                .filter(att -> att.getStatus() == AttendanceStatus.PRESENT || att.getStatus() == AttendanceStatus.LATE)
                .count();
        stats.setPresentToday(presentToday);

        // Count pending leave requests
        long pendingLeaves = leaveRequestRepository.findByStatus(LeaveStatus.PENDING).size();
        stats.setPendingLeaveRequests(pendingLeaves);

        // Department distributions
        List<Department> departments = departmentRepository.findAll();
        Map<String, Long> employeesPerDept = new HashMap<>();
        Map<String, Double> budgetPerDept = new HashMap<>();

        for (Department dept : departments) {
            long count = employeeRepository.countByDepartmentId(dept.getId());
            employeesPerDept.put(dept.getName(), count);

            // Compute total salary budget for active employees in department
            double totalSalary = dept.getEmployees().stream()
                    .filter(emp -> emp.getStatus() == EmployeeStatus.ACTIVE)
                    .mapToDouble(Employee::getSalary)
                    .sum();
            budgetPerDept.put(dept.getName(), totalSalary);
        }

        stats.setEmployeesPerDepartment(employeesPerDept);
        stats.setSalaryBudgetPerDepartment(budgetPerDept);

        return stats;
    }
}
