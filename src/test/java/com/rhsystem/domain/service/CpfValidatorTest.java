package com.rhsystem.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CpfValidatorTest {

    // ── digitsOnly ────────────────────────────────────────────────────────────

    @Test
    void digitsOnlyStripsMaskCharacters() {
        assertEquals("52998224725", CpfValidator.digitsOnly("529.982.247-25"));
        assertEquals("52998224725", CpfValidator.digitsOnly(" 529 982 247 25 "));
        assertEquals("", CpfValidator.digitsOnly("abc-"));
    }

    @Test
    void digitsOnlyOfNullIsEmpty() {
        assertEquals("", CpfValidator.digitsOnly(null));
    }

    // ── isValid ───────────────────────────────────────────────────────────────

    @ParameterizedTest
    @ValueSource(strings = {"52998224725", "529.982.247-25", "11144477735"})
    void acceptsValidCpfsWithOrWithoutMask(String cpf) {
        assertTrue(CpfValidator.isValid(cpf));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "52998224724", // wrong 2nd check digit
            "52998224735", // wrong 1st check digit
            "12345678901",
            "5299822472",   // 10 digits
            "529982247255"  // 12 digits
    })
    void rejectsInvalidCpfs(String cpf) {
        assertFalse(CpfValidator.isValid(cpf));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "00000000000", "11111111111", "22222222222", "33333333333",
            "44444444444", "55555555555", "66666666666", "77777777777",
            "88888888888", "99999999999"
    })
    void rejectsSequencesOfIdenticalDigits(String cpf) {
        assertFalse(CpfValidator.isValid(cpf));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "abc"})
    void rejectsNullEmptyAndNonNumeric(String cpf) {
        assertFalse(CpfValidator.isValid(cpf));
    }
}
