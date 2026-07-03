package com.rhsystem.application.dto.usuario;

import com.rhsystem.application.validation.CPF;
import com.rhsystem.domain.model.usuario.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Command for updating an existing user.
 * Username is immutable; password is handled by the activation flow.
 *
 * <p>Constraint messages are i18n keys, translated at the UI layer.
 */
public record UpdateUserCommand(
        @NotNull(message = "error.user.not.found")
        Long id,

        @NotBlank(message = "error.user.name.required")
        String firstName,

        @NotBlank(message = "error.user.name.required")
        String lastName,

        @NotBlank(message = "error.user.email.required")
        @Email(message = "error.email.invalid")
        String email,

        @NotBlank(message = "error.user.documents.required")
        @CPF
        String cpf,

        @NotBlank(message = "error.user.documents.required")
        String rg,

        @NotNull(message = "error.user.status.required")
        UserStatus status,

        AddressDTO address
) {
}
