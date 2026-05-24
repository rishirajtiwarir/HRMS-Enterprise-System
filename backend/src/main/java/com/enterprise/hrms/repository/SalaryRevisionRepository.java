package com.enterprise.hrms.repository;

import com.enterprise.hrms.entity.SalaryRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryRevisionRepository extends JpaRepository<SalaryRevision, Long> {
    List<SalaryRevision> findByEmployeeIdOrderByRevisionDateDesc(Long employeeId);
}
