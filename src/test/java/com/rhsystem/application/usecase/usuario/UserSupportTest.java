package com.rhsystem.application.usecase.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rhsystem.application.dto.usuario.AddressDTO;
import com.rhsystem.domain.model.usuario.Address;
import org.junit.jupiter.api.Test;

class UserSupportTest {

    @Test
    void toAddressMapsAllFields() {
        Address address = UserSupport.toAddress(
                new AddressDTO("Rua A", "Centro", "10", "apto 2", "30000-000"));

        assertEquals("Rua A", address.getStreet());
        assertEquals("Centro", address.getNeighborhood());
        assertEquals("10", address.getStreetNumber());
        assertEquals("apto 2", address.getComplement());
        assertEquals("30000-000", address.getPostalCode());
    }

    @Test
    void nullDtoBecomesEmptyAddress() {
        Address address = UserSupport.toAddress(null);
        assertNotNull(address);
        assertNull(address.getStreet());
    }

    @Test
    void isBlankHandlesNullEmptyAndWhitespace() {
        assertTrue(UserSupport.isBlank(null));
        assertTrue(UserSupport.isBlank(""));
        assertTrue(UserSupport.isBlank("   "));
        assertFalse(UserSupport.isBlank("x"));
    }

    @Test
    void alphanumericOnlyStripsSeparators() {
        assertEquals("MG12345678", UserSupport.alphanumericOnly("MG-12.345.678"));
        assertEquals("", UserSupport.alphanumericOnly(null));
        assertEquals("abc123", UserSupport.alphanumericOnly(" abc 123 "));
    }
}
