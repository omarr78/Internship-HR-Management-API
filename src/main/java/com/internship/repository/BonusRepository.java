package com.internship.repository;

import com.internship.entity.Bonus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BonusRepository extends JpaRepository<Bonus, Long> {
    List<Bonus> findByEmployeeIdAndBonusDateBetween(
            Long employeeId,
            LocalDate start,
            LocalDate end
    );

    List<Bonus> findByEmployeeIdInAndBonusDateBetween(
            List<Long> employeeIds,
            LocalDate start,
            LocalDate end
    );
}
