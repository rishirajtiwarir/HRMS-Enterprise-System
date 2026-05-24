package com.enterprise.hrms.repository;

import com.enterprise.hrms.entity.Resignation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResignationRepository extends JpaRepository<Resignation, Long> {
    List<Resignation> findByEmployeeId(Long employeeId);
}
