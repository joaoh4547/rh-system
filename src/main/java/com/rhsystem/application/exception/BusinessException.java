package com.rhsystem.application.exception;

/**
 * Exception for business rule violations (application/domain validations).
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
