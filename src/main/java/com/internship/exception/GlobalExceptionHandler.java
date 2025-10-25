package com.internship.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorCode> handleApiException(BusinessException ex){
        ErrorCode error = new ErrorCode(ex.getApiError().getHttpStatus(), ex.getMessage());

        return new ResponseEntity<>(error, ex.getApiError().getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorCode> handleAllExceptions(Exception ex){
        ErrorCode error = new ErrorCode(HttpStatus.INTERNAL_SERVER_ERROR,ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
