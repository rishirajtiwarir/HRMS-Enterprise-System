package com.enterprise.hrms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing department details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDto {
    private Long id;

    @NotBlank(message = "Department name is required")
    private String name;

    @NotBlank(message = "Department code is required")
    private String code;

    private String description;
    private Integer employeeCount;
}
