package com.internship.repository;

import com.internship.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @Query("SELECT e.salary FROM Employee e WHERE e.id = :id")
    Optional<Float> getSalary(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Employee e SET e.manager.id = :managerId WHERE e.id = :employeeId")
    void updateManager(Long employeeId, Long managerId);
}
