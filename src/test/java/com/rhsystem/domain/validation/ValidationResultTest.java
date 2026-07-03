package com.rhsystem.domain.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ValidationResultTest {

    @Test
    void emptyResultHasNoErrorsAndDoesNotThrow() {
        ValidationResult result = ValidationResult.create();
        assertFalse(result.hasErrors());
        assertDoesNotThrow(result::throwIfInvalid);
    }

    @Test
    void collectsAllViolationsAndThrowsOnce() {
        ValidationResult result = ValidationResult.create()
                .add("email", "error.user.email.required")
                .addIf(true, "cpf", "error.cpf.invalid")
                .addIf(false, "rg", "should.not.be.added")
                .requiredNotBlank("  ", "firstName", "error.user.name.required");

        assertTrue(result.hasErrors());
        assertEquals(3, result.violations().size());

        ValidationException ex = assertThrows(ValidationException.class, result::throwIfInvalid);
        assertEquals(3, ex.getViolations().size());
    }

    @Test
    void deduplicatesIdenticalViolations() {
        ValidationResult result = ValidationResult.create()
                .add("email", "error.user.email.required")
                .add("email", "error.user.email.required");
        assertEquals(1, result.violations().size());
    }

    @Test
    void mergeCombinesViolations() {
        ValidationResult a = ValidationResult.create().add("x", "key.a");
        ValidationResult b = ValidationResult.create().add("y", "key.b");
        a.merge(b);
        assertEquals(2, a.violations().size());
    }

    @Test
    void singleKeyExceptionKeepsMessageCompatibility() {
        ValidationException ex = new ValidationException("error.cpf.invalid");
        assertEquals("error.cpf.invalid", ex.getMessage());
        assertEquals(1, ex.getViolations().size());
    }
}
