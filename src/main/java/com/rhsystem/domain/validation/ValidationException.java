package com.rhsystem.domain.validation;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Signals one or more validation/business rule violations.
 *
 * <p>Carries the full list of {@link Violation}s so the UI can display every
 * error at once (Notification Pattern). {@code getMessage()} returns the
 * message keys joined by comma — for a single violation it is the key itself,
 * keeping compatibility with {@code getTranslation(ex.getMessage())}.
 */
public class ValidationException extends RuntimeException {

    private final transient List<Violation> violations;

    public ValidationException(List<Violation> violations) {
        super(violations.stream().map(Violation::messageKey).collect(Collectors.joining(", ")));
        this.violations = List.copyOf(violations);
    }

    public ValidationException(String messageKey) {
        this(List.of(Violation.of(messageKey)));
    }

    public List<Violation> getViolations() {
        return violations;
    }
}
