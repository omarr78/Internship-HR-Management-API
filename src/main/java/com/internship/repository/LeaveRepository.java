package com.internship.repository;

import com.internship.entity.Leave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface LeaveRepository extends JpaRepository<Leave, Long> {
    int countByEmployeeIdAndLeaveDateBetween(Long employeeId, LocalDate start, LocalDate end);
}