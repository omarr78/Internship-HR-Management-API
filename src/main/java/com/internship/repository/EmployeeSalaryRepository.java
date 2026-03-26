package com.internship.repository;

import com.internship.entity.EmployeeSalary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmployeeSalaryRepository extends JpaRepository<EmployeeSalary, Long> {
    @Query("""
            SELECT s FROM EmployeeSalary s
            WHERE s.employee.id IN :ids
            AND s.creationDate = (
                SELECT MAX(s2.creationDate)
                FROM EmployeeSalary s2
                WHERE s2.employee.id = s.employee.id
            )
            """)
    List<EmployeeSalary> findGrossSalaryByEmployeeIds(List<Long> ids);
}