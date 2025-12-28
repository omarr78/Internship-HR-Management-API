package com.internship.repository;

import com.internship.dto.EmployeeDtoInterface;
import com.internship.entity.Employee;
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

    @Query(
            value = """
                    WITH RECURSIVE employee_hierarchy (
                        id,
                        first_name,
                        last_name,
                        national_id,
                        degree,
                        past_experience_year,
                        joined_date,
                        date_of_birth,
                        graduation_date,
                        gender,
                        gross_salary,
                        department_id,
                        team_id,
                        manager_id
                    ) AS (
                        SELECT
                            id,
                            first_name,
                            last_name,
                            national_id,
                            degree,
                            past_experience_year,
                            joined_date,
                            date_of_birth,
                            graduation_date,
                            gender,
                            gross_salary,
                            department_id,
                            team_id,
                            manager_id
                        FROM employees
                        WHERE id = :managerId
                    
                        UNION ALL
                    
                        SELECT
                            b.id,
                            b.first_name,
                            b.last_name,
                            b.national_id,
                            b.degree,
                            b.past_experience_year,
                            b.joined_date,
                            b.date_of_birth,
                            b.graduation_date,
                            b.gender,
                            b.gross_salary,
                            b.department_id,
                            b.team_id,
                            b.manager_id
                        FROM employee_hierarchy a
                        JOIN employees b ON a.id = b.manager_id
                    )
                    SELECT
                        eh.id AS id,
                        eh.first_name AS firstName,
                        eh.last_name AS lastName,
                        eh.national_id AS nationalId,
                        eh.degree AS degree,
                        eh.past_experience_year AS pastExperienceYear,
                        eh.joined_date AS joinedDate,
                        eh.date_of_birth AS dateOfBirth,
                        eh.graduation_date AS graduationDate,
                        eh.gender AS gender,
                        eh.gross_salary AS grossSalary,
                        eh.department_id AS departmentId,
                        eh.team_id AS teamId,
                        eh.manager_id AS managerId,
                        GROUP_CONCAT(ex.name ORDER BY ex.name SEPARATOR ',') AS expertises
                    FROM employee_hierarchy eh
                    LEFT JOIN employee_expertise ee ON eh.id = ee.employee_id
                    LEFT JOIN expertises ex ON ee.expertise_id = ex.id
                    WHERE eh.id != :managerId
                    GROUP BY
                        eh.id,
                        eh.first_name,
                        eh.last_name,
                        eh.national_id,
                        eh.degree,
                        eh.past_experience_year,
                        eh.joined_date,
                        eh.date_of_birth,
                        eh.graduation_date,
                        eh.gender,
                        eh.gross_salary,
                        eh.department_id,
                        eh.team_id,
                        eh.manager_id;
                    """,
            nativeQuery = true
    )
    List<EmployeeDtoInterface> getAllEmployeesUnderManager(@Param("managerId") Long managerId);

    @EntityGraph(attributePaths = {"department", "team", "expertises"})
    List<Employee> findByManagerId(Long managerId);
}
