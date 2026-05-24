package com.enterprise.hrms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Data Transfer Object representing employee profiles.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {
    private Long id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phone;
    private LocalDate dateOfBirth;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    @NotBlank(message = "Designation is required")
    private String designation;

    private String status;
    private String profileImage;

    @NotNull(message = "Salary is required")
    private Double salary;

    private Long departmentId;
    private String departmentName;

    private Long managerId;
    private String managerName;

    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;

    // Optional user account details
    private String username;
    private String userRole;
}
