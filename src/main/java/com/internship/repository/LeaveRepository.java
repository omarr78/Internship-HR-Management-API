package com.internship.repository;

import com.internship.entity.Leave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findByEmployeeIdAndLeaveDateBetweenOrderByLeaveDateAsc(
            Long employeeId,
            LocalDate start,
            LocalDate end
    );

    List<Leave> findByEmployeeIdInAndLeaveDateBetween(
            List<Long> employeeIds,
            LocalDate start,
            LocalDate end
    );
}