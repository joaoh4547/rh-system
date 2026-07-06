package com.rhsystem.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

class UsernameGeneratorTest {

    private static final Predicate<String> NEVER_EXISTS = u -> false;

    @Test
    void joinsFirstAndLastNameLowercase() {
        assertEquals("joao.silva", UsernameGenerator.generate("João", "Silva", NEVER_EXISTS));
    }

    @Test
    void stripsAccentsAndSpecialCharacters() {
        assertEquals("jose.conceicao",
                UsernameGenerator.generate("José", "Conceição!", NEVER_EXISTS));
    }

    @Test
    void multiWordFirstNameUsesFirstAndLastWordOfFirstName() {
        // "João Henrique" + "Teixeira" → joao.henrique
        assertEquals("joao.henrique",
                UsernameGenerator.generate("João Henrique", "Teixeira", NEVER_EXISTS));
    }

    @Test
    void skipsConnectivesInLastName() {
        assertEquals("maria.souza", UsernameGenerator.generate("Maria", "de Souza", NEVER_EXISTS));
        assertEquals("joao.jesus",
                UsernameGenerator.generate("João", "Teixeira de Jesus", NEVER_EXISTS));
    }

    @Test
    void lastNameMadeOnlyOfConnectivesFallsBackToLastToken() {
        assertEquals("ana.de", UsernameGenerator.generate("Ana", "de", NEVER_EXISTS));
    }

    @Test
    void emptyLastNameUsesOnlyFirstName() {
        assertEquals("joao", UsernameGenerator.generate("João", "", NEVER_EXISTS));
        assertEquals("joao", UsernameGenerator.generate("João", null, NEVER_EXISTS));
    }

    @Test
    void emptyFirstNameUsesOnlyLastName() {
        assertEquals("silva", UsernameGenerator.generate("", "Silva", NEVER_EXISTS));
    }

    @Test
    void blankNamesFallBackToUser() {
        assertEquals("user", UsernameGenerator.generate("  ", null, NEVER_EXISTS));
    }

    @Test
    void appendsNumericSuffixOnCollision() {
        Predicate<String> taken = Set.of("joao.silva")::contains;
        assertEquals("joao.silva1", UsernameGenerator.generate("João", "Silva", taken));
    }

    @Test
    void incrementsSuffixWhileTaken() {
        Predicate<String> taken = Set.of("joao.silva", "joao.silva1", "joao.silva2")::contains;
        assertEquals("joao.silva3", UsernameGenerator.generate("João", "Silva", taken));
    }
}
