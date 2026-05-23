package com.enterprise.hrms.controller;

import com.enterprise.hrms.dto.DashboardStatsDto;
import com.enterprise.hrms.dto.EmployeeDto;
import com.enterprise.hrms.dto.MessageResponse;
import com.enterprise.hrms.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Controller managing Employee profile operations, search, pagination, and file uploads.
 */
@RestController
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    // Retrieve a page of employees (ADMIN, HR, and EMPLOYEE can view directory)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('EMPLOYEE')")
    public ResponseEntity<Page<EmployeeDto>> getAllEmployees(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        
        Page<EmployeeDto> employees = employeeService.getAllEmployees(search, page, size, sortBy, sortDir);
        return ResponseEntity.ok(employees);
    }

    // Retrieve dashboard stats (ADMIN or HR only)
    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        DashboardStatsDto stats = employeeService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    // Get specific employee by ID (Authenticated user)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or #id == authentication.principal.employeeId")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long id) {
        EmployeeDto employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    // Create a new employee (ADMIN or HR)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody EmployeeDto employeeDto) {
        EmployeeDto createdEmployee = employeeService.createEmployee(employeeDto);
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }

    // Update employee profile details (ADMIN, HR, or self-update)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or #id == authentication.principal.employeeId")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeDto employeeDto) {
        EmployeeDto updatedEmployee = employeeService.updateEmployee(id, employeeDto);
        return ResponseEntity.ok(updatedEmployee);
    }

    // Deactivate/Delete employee profile (ADMIN or HR)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(new MessageResponse("Employee deactivated successfully!"));
    }

    // Upload employee profile photo
    @PostMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or #id == authentication.principal.employeeId")
    public ResponseEntity<?> uploadProfileImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Please select a file to upload."));
        }

        try {
            // Create uploads directory if not exists
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Extract file extension and build name
            String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
            String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String fileName = "employee_" + id + "_" + System.currentTimeMillis() + extension;

            Path targetPath = Paths.get(uploadDir).resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Save the file path/URL to the employee profile (using a simple serving url schema)
            String profileImageUrl = "/api/v1/employees/images/" + fileName;
            EmployeeDto updatedEmployee = employeeService.updateProfileImage(id, profileImageUrl);

            return ResponseEntity.ok(updatedEmployee);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Failed to upload file: " + e.getMessage()));
        }
    }

    // Endpoint to serve uploaded profile images (Permit-all via security filters can be toggled, but security context takes care)
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<byte[]> serveFile(@PathVariable String filename) {
        try {
            Path file = Paths.get(uploadDir).resolve(filename);
            byte[] bytes = Files.readAllBytes(file);
            
            // Deduce mime type
            String contentType = Files.probeContentType(file);
            if (contentType == null) {
                contentType = "image/jpeg";
            }

            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .body(bytes);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
