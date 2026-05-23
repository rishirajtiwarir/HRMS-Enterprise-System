package com.enterprise.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Data Transfer Object representing attendance logs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate date;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private String status;
    private Double workHours;
}
