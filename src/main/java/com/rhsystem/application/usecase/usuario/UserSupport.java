package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.dto.usuario.AddressDTO;
import com.rhsystem.domain.model.usuario.Address;

/**
 * Utility functions shared across User use cases.
 *
 * <p>Validation lives in the command annotations ({@code jakarta.validation})
 * combined with {@link com.rhsystem.application.validation.CommandValidator}
 * and {@link com.rhsystem.domain.validation.ValidationResult}.
 */
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
}
