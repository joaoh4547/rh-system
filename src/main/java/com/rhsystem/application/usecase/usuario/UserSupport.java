package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.dto.usuario.ActivationCommand;
import com.rhsystem.application.dto.usuario.AddressDTO;
import com.rhsystem.application.dto.usuario.CreateUserCommand;
import com.rhsystem.application.exception.BusinessException;
import com.rhsystem.domain.model.usuario.Address;

/** Utility functions shared across User use cases. */
final class UserSupport {

    private UserSupport() {
    }

    static Address toAddress(AddressDTO dto) {
        if (dto == null) {
            return new Address();
        }
        return new Address(dto.street(), dto.neighborhood(), dto.streetNumber(), dto.complement(), dto.postalCode());
    }

    static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    static String alphanumericOnly(String value) {
        return value == null ? "" : value.replaceAll("[^A-Za-z0-9]", "");
    }

    static void validateRequired(CreateUserCommand cmd, String cpf, String rg) {
        if (isBlank(cmd.firstName()) || isBlank(cmd.lastName())) {
            throw new BusinessException("error.user.name.required");
        }
        if (isBlank(cmd.email())) {
            throw new BusinessException("error.user.email.required");
        }
        if (isBlank(cpf) || isBlank(rg)) {
            throw new BusinessException("error.user.documents.required");
        }
    }

    static void validatePassword(ActivationCommand cmd) {
        if (cmd.password() == null || cmd.password().length() < 6) {
            throw new BusinessException("error.password.too.short");
        }
        if (!cmd.password().equals(cmd.passwordConfirmation())) {
            throw new BusinessException("error.password.mismatch");
        }
    }
}
