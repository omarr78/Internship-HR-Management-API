package com.internship.exception;

import org.springframework.http.HttpStatus;

public enum ApiError {

    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Department not found");

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
