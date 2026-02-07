package com.internship.repository;

import com.internship.entity.EmployeeSalary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeSalaryRepository extends JpaRepository<EmployeeSalary, Long> {
    EmployeeSalary findTopByEmployeeIdOrderByCreationDateDesc(Long employeeId);
}