package com.rhsystem.application.validation;

import com.rhsystem.domain.validation.ValidationResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

/**
 * Bridges Bean Validation (structural rules declared as annotations on
 * commands) with the domain Notification Pattern ({@link ValidationResult}).
 *
 * <p>Constraint messages are i18n keys; they are collected as-is into
 * {@link com.rhsystem.domain.validation.Violation}s and translated at the UI.
 *
 * <p>Usage in a use case:
 * <pre>{@code
 * ValidationResult result = commandValidator.check(cmd); // structural rules
 * result.addIf(repo.existsByEmail(cmd.email()), "email", "error.user.email.duplicate"); // business rules
 * result.throwIfInvalid(); // throws ValidationException with ALL violations
 * }</pre>
 */
@Component
public class CommandValidator {

    private final Validator validator;

    public CommandValidator(Validator validator) {
        this.validator = validator;
    }

    /** Runs the annotated constraints and returns all violations without throwing. */
    public <T> ValidationResult check(T command) {
        ValidationResult result = ValidationResult.create();
        for (ConstraintViolation<T> violation : validator.validate(command)) {
            result.add(violation.getPropertyPath().toString(), violation.getMessage());
        }
        return result;
    }

    /** Runs the annotated constraints and throws if any violation is found. */
    public <T> void validate(T command) {
        check(command).throwIfInvalid();
    }
}
