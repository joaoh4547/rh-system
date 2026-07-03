package com.rhsystem.domain.validation;

/**
 * A single validation violation.
 *
 * @param field      logical name of the offending field ({@code null} for object-level rules)
 * @param messageKey i18n message key, translated at the UI layer
 */
public record Violation(String field, String messageKey) {

    /** Creates an object-level violation (not tied to a specific field). */
    public static Violation of(String messageKey) {
        return new Violation(null, messageKey);
    }
}
