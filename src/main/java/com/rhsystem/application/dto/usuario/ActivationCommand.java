package com.rhsystem.application.dto.usuario;

/**
 * Command for account activation: token received by email + new password.
 */
public record ActivationCommand(
        String token,
        String password,
        String passwordConfirmation
) {
}
