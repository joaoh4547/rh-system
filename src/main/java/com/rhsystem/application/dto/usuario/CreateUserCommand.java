package com.rhsystem.application.dto.usuario;

import com.rhsystem.application.validation.CPF;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Command for creating a new user.
 * The password is not provided here — it is set during email activation.
 *
 * <p>Constraint messages are i18n keys, translated at the UI layer.
 */
public record CreateUserCommand(
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

        AddressDTO address,
        List<DocumentUpload> documents
) {
}
