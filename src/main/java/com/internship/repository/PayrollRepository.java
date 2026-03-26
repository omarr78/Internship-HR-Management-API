package com.internship.repository;

import com.internship.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    List<Payroll> findByEmployeeIdInAndPayrollYearAndPayrollMonth(
            List<Long> employeeIds,
            Integer payrollYear,
            Integer payrollMonth
    );
}
