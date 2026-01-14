package com.internship.validation.aspect;

import com.internship.dto.CreateEmployeeRequest;
import com.internship.dto.UpdateEmployeeRequest;
import com.internship.entity.Employee;
import com.internship.exception.BusinessException;
import com.internship.repository.EmployeeRepository;
import lombok.AllArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

import static com.internship.exception.ApiError.INVALID_EMPLOYEE_DATES_EXCEPTION;

@Aspect
@Component
@AllArgsConstructor
public class EmployeeValidatorAspect {
    private static final int MAX_DIFFERENCE_YEARS = 20;
    private EmployeeRepository employeeRepository;

    private void validateGraduationAndBirthDate(LocalDate dateOfBirth, LocalDate graduationDate) {
        if (dateOfBirth != null && graduationDate != null) {
            int years = Period.between(dateOfBirth, graduationDate).getYears();
            if (years < MAX_DIFFERENCE_YEARS) {
                throw new BusinessException(INVALID_EMPLOYEE_DATES_EXCEPTION);
            }
        }
    }

    @Before("@annotation(com.internship.validation.aspect.ValidateCreateRequest)")
    public void validateCreate(JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof CreateEmployeeRequest request) {
                validateGraduationAndBirthDate(request.getDateOfBirth(), request.getGraduationDate());
            }
        }
    }

    @Before("@annotation(com.internship.validation.aspect.ValidateUpdateRequest)")
    public void validateBeforeSave(JoinPoint joinPoint) {
        UpdateEmployeeRequest updateRequest = null;
        Long employeeId = null;
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof UpdateEmployeeRequest request) {
                updateRequest = request;
            }
            if (arg instanceof Long id) {
                employeeId = id;
            }
        }
        if (updateRequest != null && employeeId != null) {
            Optional<Employee> employee = employeeRepository.findById(employeeId);

            if (employee.isPresent()) {
                LocalDate dob = updateRequest.getDateOfBirth() != null
                        ? updateRequest.getDateOfBirth()
                        : employee.get().getDateOfBirth();

                LocalDate grad = updateRequest.getGraduationDate() != null
                        ? updateRequest.getGraduationDate()
                        : employee.get().getGraduationDate();

                validateGraduationAndBirthDate(dob, grad);
            }
        }
    }
}
