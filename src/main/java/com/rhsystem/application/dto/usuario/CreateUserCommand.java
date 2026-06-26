package com.rhsystem.application.dto.usuario;

import java.util.List;

/**
 * Command for creating a new user.
 * The password is not provided here — it is set during email activation.
 */
public record CreateUserCommand(
        String firstName,
        String lastName,
        String email,
        String cpf,
        String rg,
        AddressDTO address,
        List<DocumentUpload> documents
) {
}
