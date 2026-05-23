package com.enterprise.hrms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

/**
 * Data Transfer Object for creating an employee and user login account together.
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6)
    private String password;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    private Set<String> roles;

    // Employee specific profile fields
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String phone;

    private LocalDate dateOfBirth;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    @NotBlank(message = "Designation is required")
    private String designation;

    @NotNull(message = "Base salary is required")
    private Double salary;

    private Long departmentId;
}
