package com.rhsystem.application.dto.usuario;

import com.rhsystem.domain.service.PasswordPolicy;
import com.rhsystem.application.validation.FieldsMatch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Command for account activation: token received by email + new password.
 * Also reused by the password reset flow.
 *
 * <p>Constraint messages are i18n keys, translated at the UI layer.
 */
@FieldsMatch(first = "password", second = "passwordConfirmation",
        message = "error.password.mismatch")
public record ActivationCommand(
        String token,

        @NotBlank(message = "error.password.required")
        // 8 = NIST SP 800-63B minimum; 72 = BCrypt input limit (longer input is rejected by the encoder)
        @Size(min = PasswordPolicy.MIN_LENGTH, max = PasswordPolicy.MAX_LENGTH, message = "error.password.too.short")
        String password,

        String passwordConfirmation
) {
}
