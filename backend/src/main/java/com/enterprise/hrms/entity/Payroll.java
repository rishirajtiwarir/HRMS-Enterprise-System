package com.enterprise.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

/**
 * JPA entity representing payroll information for employees.
 */
@Entity
@Table(name = "payrolls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull(message = "Pay period start date is required")
    @Column(name = "pay_period_start", nullable = false)
    private LocalDate payPeriodStart;

    @NotNull(message = "Pay period end date is required")
    @Column(name = "pay_period_end", nullable = false)
    private LocalDate payPeriodEnd;

    @NotNull(message = "Basic salary is required")
    @Column(name = "basic_salary", nullable = false)
    private Double basicSalary;

    private Double allowances = 0.0;

    private Double deductions = 0.0;

    @Column(name = "net_salary", nullable = false)
    private Double netSalary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayrollStatus status = PayrollStatus.PENDING;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "payslip_pdf_path")
    private String payslipPdfPath;

    @PrePersist
    @PreUpdate
    protected void calculateNetSalary() {
        if (this.allowances == null) this.allowances = 0.0;
        if (this.deductions == null) this.deductions = 0.0;
        if (this.basicSalary == null) this.basicSalary = 0.0;
        this.netSalary = this.basicSalary + this.allowances - this.deductions;
    }
}
