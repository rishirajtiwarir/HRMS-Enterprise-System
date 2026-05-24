package com.enterprise.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "department_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "previous_department_id")
    private Department previousDepartment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "new_department_id", nullable = false)
    private Department newDepartment;

    @NotNull(message = "Transfer date is required")
    @Column(name = "transfer_date", nullable = false)
    private LocalDate transferDate;

    @Column(length = 500)
    private String reason;
}
