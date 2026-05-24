package com.enterprise.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "employee_promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePromotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotBlank(message = "Previous designation is required")
    @Column(name = "previous_designation", nullable = false)
    private String previousDesignation;

    @NotBlank(message = "New designation is required")
    @Column(name = "new_designation", nullable = false)
    private String newDesignation;

    @NotNull(message = "Previous salary is required")
    @Column(name = "previous_salary", nullable = false)
    private Double previousSalary;

    @NotNull(message = "New salary is required")
    @Column(name = "new_salary", nullable = false)
    private Double newSalary;

    @NotNull(message = "Promotion date is required")
    @Column(name = "promotion_date", nullable = false)
    private LocalDate promotionDate;
}
