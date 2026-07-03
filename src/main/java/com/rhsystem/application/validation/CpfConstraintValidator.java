package com.rhsystem.application.validation;

import com.rhsystem.domain.service.CpfValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Bean Validation adapter for the domain {@link CpfValidator}. */
public class CpfConstraintValidator implements ConstraintValidator<CPF, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // presence is @NotBlank's responsibility
        }
        return CpfValidator.isValid(value);
    }
}
