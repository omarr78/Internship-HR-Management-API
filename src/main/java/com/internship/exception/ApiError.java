package com.internship.exception;

import org.springframework.http.HttpStatus;

public enum ApiError {

    SELF_MANAGEMENT(HttpStatus.BAD_REQUEST, "Employee cannot be self management"),
    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "Employee not found"),
    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Department not found"),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "Team not found"),
    INVALID_EMPLOYEE_DATES_EXCEPTION(HttpStatus.BAD_REQUEST, "graduation date must be after birth date at least 20 years"),
    INVALID_EMPLOYEE_REMOVAL(HttpStatus.BAD_REQUEST, "Cannot remove manager (employee has subordinates) has no manager");

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
