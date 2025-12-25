package com.internship.dto;

import com.internship.enums.Degree;
import com.internship.enums.Gender;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeDtoInterface {
    Long getId();

    String getFirstName();

    String getLastName();

    String getNationalId();

    Degree getDegree();

    Integer getPastExperienceYear();

    LocalDate getJoinedDate();

    LocalDate getDateOfBirth();

    LocalDate getGraduationDate();

    Gender getGender();

    float getGrossSalary();

    Long getDepartmentId();

    Long getTeamId();

    Long getManagerId();

    List<String> getExpertises();
}
