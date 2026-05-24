package com.enterprise.hrms.repository;

import com.enterprise.hrms.entity.EmployeePromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeePromotionRepository extends JpaRepository<EmployeePromotion, Long> {
    List<EmployeePromotion> findByEmployeeIdOrderByPromotionDateDesc(Long employeeId);
}
