package com.enterprise.hrms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing corporate departments.
 */
@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Department name is required")
    @Column(unique = true, nullable = false)
    private String name;

    @NotBlank(message = "Department code is required")
    @Column(unique = true, nullable = false)
    private String code;

    private String description;

    // A department can have multiple employees
    @OneToMany(mappedBy = "department", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JsonIgnore // Prevent infinite recursion during JSON serialization
    private List<Employee> employees = new ArrayList<>();
}
