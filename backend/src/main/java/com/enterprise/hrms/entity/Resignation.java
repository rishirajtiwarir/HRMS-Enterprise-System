package com.enterprise.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "resignations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Resignation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotBlank(message = "Reason for resignation is required")
    @Column(nullable = false, length = 1000)
    private String reason;

    @NotNull(message = "Submission date is required")
    @Column(name = "submission_date", nullable = false)
    private LocalDate submissionDate;

    @NotNull(message = "Last working date is required")
    @Column(name = "last_working_date", nullable = false)
    private LocalDate lastWorkingDate;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "feedback")
    private String feedback;
}
