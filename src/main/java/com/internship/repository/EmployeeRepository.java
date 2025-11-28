package com.internship.repository;

import com.internship.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Employee e SET e.manager.id = :managerId WHERE e.id = :employeeId")
    void updateManager(Long employeeId, Long managerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Employee AS e SET e.manager.id = :newManager WHERE e.manager.id = :oldManager")
    void reassignManager(@Param("oldManager") Long oldManager,
                         @Param("newManager") Long newManager);
}
