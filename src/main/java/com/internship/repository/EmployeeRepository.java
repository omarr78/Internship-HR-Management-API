package com.internship.repository;

import com.internship.entity.Employee;
import com.internship.entity.Team;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Employee AS e SET e.manager.id = :newManager WHERE e.manager.id = :oldManager")
    void reassignManager(@Param("oldManager") Long oldManager,
                         @Param("newManager") Long newManager);

    // to avoid N + 1 problem
    @EntityGraph(attributePaths = "expertises")
    List<Employee> findAllByTeam(Team team);
}
