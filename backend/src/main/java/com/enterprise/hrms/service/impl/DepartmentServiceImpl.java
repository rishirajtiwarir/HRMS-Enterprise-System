package com.enterprise.hrms.service.impl;

import com.enterprise.hrms.dto.DepartmentDto;
import com.enterprise.hrms.entity.Department;
import com.enterprise.hrms.exception.BadRequestException;
import com.enterprise.hrms.exception.ResourceNotFoundException;
import com.enterprise.hrms.mapper.DepartmentMapper;
import com.enterprise.hrms.repository.DepartmentRepository;
import com.enterprise.hrms.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(departmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDto getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));
        return departmentMapper.toDto(department);
    }

    @Override
    @Transactional
    public DepartmentDto createDepartment(DepartmentDto departmentDto) {
        if (departmentRepository.findByName(departmentDto.getName()).isPresent()) {
            throw new BadRequestException("Department name already exists!");
        }
        if (departmentRepository.findByCode(departmentDto.getCode()).isPresent()) {
            throw new BadRequestException("Department code already exists!");
        }

        Department department = departmentMapper.toEntity(departmentDto);
        Department savedDepartment = departmentRepository.save(department);
        return departmentMapper.toDto(savedDepartment);
    }

    @Override
    @Transactional
    public DepartmentDto updateDepartment(Long id, DepartmentDto departmentDto) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));

        // Name check
        departmentRepository.findByName(departmentDto.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BadRequestException("Department name already exists!");
            }
        });

        // Code check
        departmentRepository.findByCode(departmentDto.getCode()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BadRequestException("Department code already exists!");
            }
        });

        department.setName(departmentDto.getName());
        department.setCode(departmentDto.getCode());
        department.setDescription(departmentDto.getDescription());

        Department updatedDepartment = departmentRepository.save(department);
        return departmentMapper.toDto(updatedDepartment);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));

        // Detach employees before deleting, or block deletion if it contains employees
        if (department.getEmployees() != null && !department.getEmployees().isEmpty()) {
            // Remove department link from employees instead of cascade deleting them
            department.getEmployees().forEach(employee -> employee.setDepartment(null));
        }

        departmentRepository.delete(department);
    }
}
