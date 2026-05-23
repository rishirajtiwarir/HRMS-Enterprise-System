package com.enterprise.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Data Transfer Object representing aggregate statistics for the admin dashboard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private Long totalEmployees;
    private Long totalDepartments;
    private Long activeEmployees;
    private Long pendingLeaveRequests;
    private Long presentToday;
    private Map<String, Long> employeesPerDepartment;
    private Map<String, Double> salaryBudgetPerDepartment;
}
