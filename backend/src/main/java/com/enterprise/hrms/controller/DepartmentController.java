package com.enterprise.hrms.controller;

import com.enterprise.hrms.dto.DepartmentDto;
import com.enterprise.hrms.dto.MessageResponse;
import com.enterprise.hrms.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller managing department configurations.
 */
@RestController
@RequestMapping("/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    // Get all departments (Authenticated users)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('EMPLOYEE')")
    public ResponseEntity<List<DepartmentDto>> getAllDepartments() {
        List<DepartmentDto> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    // Get department details
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('EMPLOYEE')")
    public ResponseEntity<DepartmentDto> getDepartmentById(@PathVariable Long id) {
        DepartmentDto department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(department);
    }

    // Create a department (ADMIN or HR)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<DepartmentDto> createDepartment(@Valid @RequestBody DepartmentDto departmentDto) {
        DepartmentDto createdDepartment = departmentService.createDepartment(departmentDto);
        return new ResponseEntity<>(createdDepartment, HttpStatus.CREATED);
    }

    // Update a department (ADMIN or HR)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<DepartmentDto> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentDto departmentDto) {
        DepartmentDto updatedDepartment = departmentService.updateDepartment(id, departmentDto);
        return ResponseEntity.ok(updatedDepartment);
    }

    // Delete a department (ADMIN or HR)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(new MessageResponse("Department deleted successfully!"));
    }
}
