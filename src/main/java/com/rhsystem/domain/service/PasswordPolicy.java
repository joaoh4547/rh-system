package com.rhsystem.domain.service;

import com.rhsystem.domain.validation.ValidationResult;
import java.util.Locale;
import java.util.Set;

/**
 * Domain service: password rules that need context (the user) or a blocklist.
 *
 * <p>Length (8..72) is checked by Bean Validation on the command — 72 is BCrypt's
 * input limit. Here: reject well-known passwords and passwords equal to the user's
 * username or email (NIST SP 800-63B: check against commonly used / context-specific
 * values instead of composition rules).</p>
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 72;

    /** Small local blocklist of the most common passwords (lower-case). */
    private static final Set<String> COMMON = Set.of(
            "12345678", "123456789", "1234567890", "12345678910", "123123123", "11111111",
            "00000000", "87654321", "88888888", "99999999", "abcd1234", "abc12345",
            "password", "password1", "password123", "passw0rd", "qwerty123", "qwertyuiop",
            "iloveyou", "sunshine", "princess", "football", "baseball", "welcome1",
            "admin123", "administrator", "letmein1", "trustno1", "1q2w3e4r", "1qaz2wsx",
            "senha123", "senha1234", "minhasenha", "mudar123", "mudar@123", "brasil123",
            "flamengo", "corinthians", "palmeiras", "123mudar", "teste123", "q1w2e3r4");

    private PasswordPolicy() {
    }

    /**
     * @return violations on field {@code password} (empty result if acceptable)
     */
    public static ValidationResult check(String password, String username, String email) {
        ValidationResult result = ValidationResult.create();
        if (password == null || password.isBlank()) {
            return result; // "required" is the command validator's job
        }
        String candidate = password.trim().toLowerCase(Locale.ROOT);
        result.addIf(COMMON.contains(candidate), "password", "error.password.common");
        result.addIf(matches(candidate, username) || matches(candidate, email)
                        || matches(candidate, localPart(email)),
                "password", "error.password.personal");
        return result;
    }

    private static boolean matches(String candidate, String value) {
        return value != null && !value.isBlank() && candidate.equals(value.trim().toLowerCase(Locale.ROOT));
    }

    private static String localPart(String email) {
        if (email == null) {
            return null;
        }
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : null;
    }
}
