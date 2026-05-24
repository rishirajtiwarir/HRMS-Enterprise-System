package com.enterprise.hrms.repository;

import com.enterprise.hrms.entity.DepartmentTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentTransferRepository extends JpaRepository<DepartmentTransfer, Long> {
    List<DepartmentTransfer> findByEmployeeIdOrderByTransferDateDesc(Long employeeId);
}
