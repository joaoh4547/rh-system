package com.rhsystem.domain.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Notification Pattern: accumulates {@link Violation}s instead of failing on
 * the first error, then throws a single {@link ValidationException} carrying
 * all of them.
 *
 * <pre>{@code
 * ValidationResult.create()
 *         .requiredNotBlank(cmd.email(), "email", "error.user.email.required")
 *         .addIf(repo.existsByEmail(cmd.email()), "email", "error.user.email.duplicate")
 *         .throwIfInvalid();
 * }</pre>
 */
public final class ValidationResult {

    private final List<Violation> violations = new ArrayList<>();

    private ValidationResult() {
    }

    public static ValidationResult create() {
        return new ValidationResult();
    }

    public ValidationResult add(Violation violation) {
        if (!violations.contains(violation)) {
            violations.add(violation);
        }
        return this;
    }

    public ValidationResult add(String field, String messageKey) {
        return add(new Violation(field, messageKey));
    }

    public ValidationResult add(String messageKey) {
        return add(Violation.of(messageKey));
    }

    /** Adds the violation only when {@code condition} is true. */
    public ValidationResult addIf(boolean condition, String field, String messageKey) {
        return condition ? add(field, messageKey) : this;
    }

    /** Adds the violation when the value is null or blank. */
    public ValidationResult requiredNotBlank(String value, String field, String messageKey) {
        return addIf(value == null || value.isBlank(), field, messageKey);
    }

    /** Merges another result's violations into this one. */
    public ValidationResult merge(ValidationResult other) {
        other.violations.forEach(this::add);
        return this;
    }

    public boolean hasErrors() {
        return !violations.isEmpty();
    }

    public List<Violation> violations() {
        return Collections.unmodifiableList(violations);
    }

    /** Throws {@link ValidationException} with all violations, if any. */
    public void throwIfInvalid() {
        if (hasErrors()) {
            throw new ValidationException(violations);
        }
    }
}
