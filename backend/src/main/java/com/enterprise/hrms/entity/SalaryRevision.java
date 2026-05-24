package com.enterprise.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "salary_revisions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalaryRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull(message = "Previous salary is required")
    @Column(name = "previous_salary", nullable = false)
    private Double previousSalary;

    @NotNull(message = "New salary is required")
    @Column(name = "new_salary", nullable = false)
    private Double newSalary;

    @NotNull(message = "Revision date is required")
    @Column(name = "revision_date", nullable = false)
    private LocalDate revisionDate;

    @Column(name = "revised_by", nullable = false)
    private String revisedBy;
}
