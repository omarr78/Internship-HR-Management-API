package com.internship.exception;

import org.springframework.http.HttpStatus;

public enum ApiError {
    INVALID_DATE_YEAR(HttpStatus.BAD_REQUEST, "date must be in the same current year"),
    INVALID_DATE_MONTH(HttpStatus.BAD_REQUEST, "date must be at least in the same current month"),
    INVALID_DATA(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid data entered"),
    HIERARCHY_CYCLE_DETECTED(HttpStatus.CONFLICT, "Cycle detected in employee hierarchy"),
    NEGATIVE_SALARY(HttpStatus.CONFLICT, "Salary cannot be Negative after deduction"),
    SELF_MANAGEMENT(HttpStatus.BAD_REQUEST, "Employee cannot be self management"),
    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "Employee not found"),
    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Department not found"),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "Team not found"),
    INVALID_EMPLOYEE_DATES_EXCEPTION(HttpStatus.BAD_REQUEST, "graduation date must be after birth date at least 20 years"),
    INVALID_EMPLOYEE_REMOVAL(HttpStatus.CONFLICT, "Cannot remove manager (employee has subordinates) has no manager");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ApiError(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
