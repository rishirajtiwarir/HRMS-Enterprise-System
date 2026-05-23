package com.enterprise.hrms.repository;

import com.enterprise.hrms.entity.Employee;
import com.enterprise.hrms.entity.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);

    // Search and filter with pagination
    Page<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String firstName, String lastName, String email, Pageable pageable);

    long countByStatus(EmployeeStatus status);

    long countByDepartmentId(Long departmentId);
}
