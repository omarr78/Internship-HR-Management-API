package com.internship.exception;

public class BusinessException extends RuntimeException {
    private final ApiError apiError;

    public BusinessException(ApiError apiError, String message) {
        super(message);
        this.apiError = apiError;
    }

    public BusinessException(ApiError apiError) {
        super(apiError.getDefaultMessage());
        this.apiError = apiError;
    }

    public ApiError getApiError() {
        return apiError;
    }
}
