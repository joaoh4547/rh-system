package com.rhsystem.application.dto.usuario;

import com.rhsystem.domain.model.usuario.UserStatus;

/**
 * Command for updating an existing user.
 * Username is immutable; password is handled by the activation flow.
 */
public record UpdateUserCommand(
        Long id,
        String firstName,
        String lastName,
        String email,
        String cpf,
        String rg,
        UserStatus status,
        AddressDTO address
) {
}
