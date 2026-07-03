package com.rhsystem.application.exception;

import com.rhsystem.domain.validation.ValidationException;

/**
 * Exception for a single business rule violation (message = i18n key).
 *
 * <p>Extends {@link ValidationException}, so UI layers can handle both with a
 * single {@code catch (ValidationException)} block.
 */
public class BusinessException extends ValidationException {
    public BusinessException(String messageKey) {
        super(messageKey);
    }
}
