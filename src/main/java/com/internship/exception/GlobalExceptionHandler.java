package com.internship.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorCode> handleApiException(BusinessException ex) {
        ErrorCode error = new ErrorCode(ex.getApiError().getHttpStatus(), ex.getMessage());

        return new ResponseEntity<>(error, ex.getApiError().getHttpStatus());
    }

    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                          HttpHeaders headers,
                                                                          HttpStatusCode status,
                                                                          WebRequest request) {
        String name = ex.getParameterName();
        ErrorCode error = new ErrorCode(HttpStatus.BAD_REQUEST, name + " parameter is missing");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public final ResponseEntity<ErrorCode> handleDuplicateEntry(DataIntegrityViolationException ex) {
        Throwable root = ex.getRootCause();
        // First try Hibernate's ConstraintViolationException which exposes the constraint name
        if (root instanceof org.hibernate.exception.ConstraintViolationException cve) {
            String constraintName = cve.getConstraintName();
            if (constraintName != null && constraintName.toUpperCase().contains("UQ_LEAVES")) {
                ErrorCode errorDetails = new ErrorCode(
                        HttpStatus.CONFLICT,
                        "This employee already has a leave recorded for the specified date"
                );
                return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT); // 409 Conflict
            }
        }

        // Some DB drivers (H2/MySQL/Postgres) throw their own SQLExceptions; inspect the message
        String rootMessage = null;
        if (root != null && root.getMessage() != null) {
            rootMessage = root.getMessage().toUpperCase();
        }
        if (rootMessage != null && rootMessage.contains("UQ_LEAVES")) {
            ErrorCode errorDetails = new ErrorCode(
                    HttpStatus.CONFLICT,
                    "This employee already has a leave recorded for the specified date"
            );
            return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT); // 409 Conflict
        }

        // Generic database error
        ErrorCode errorDetails = new ErrorCode(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Database integrity error"
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorCode> handleAllExceptions(Exception ex) {
        ErrorCode error = new ErrorCode(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ErrorCode errorDetails = new ErrorCode(HttpStatus.BAD_REQUEST,
                Objects.requireNonNull(ex.getFieldError()).getDefaultMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
}