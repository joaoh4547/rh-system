package com.rhsystem.application.dto.usuario;

/**
 * Address data used in use cases.
 */
public record AddressDTO(
        String street,
        String neighborhood,
        String streetNumber,
        String complement,
        String postalCode
) {
}
