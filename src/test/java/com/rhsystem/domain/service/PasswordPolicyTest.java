package com.rhsystem.domain.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rhsystem.domain.validation.Violation;
import java.util.List;
import org.junit.jupiter.api.Test;

class PasswordPolicyTest {

    private static List<String> keys(String password, String username, String email) {
        return PasswordPolicy.check(password, username, email).violations().stream()
                .map(Violation::messageKey).toList();
    }

    @Test
    void commonPasswordsAreRejectedCaseInsensitively() {
        assertTrue(keys("Senha123", "joao.silva", "joao@example.com").contains("error.password.common"));
        assertTrue(keys("12345678", null, null).contains("error.password.common"));
    }

    @Test
    void passwordEqualToUsernameEmailOrEmailLocalPartIsRejected() {
        assertTrue(keys("JOAO.SILVA", "joao.silva", "x@y.com").contains("error.password.personal"));
        assertTrue(keys("joao@example.com", "j", "joao@example.com").contains("error.password.personal"));
        assertTrue(keys("joaozinho", "j", "joaozinho@example.com").contains("error.password.personal"));
    }

    @Test
    void reasonablePasswordPasses() {
        assertFalse(PasswordPolicy.check("cavalo-bateria-grampo", "joao.silva", "joao@example.com").hasErrors());
    }

    @Test
    void blankOrNullIsLeftToBeanValidation() {
        assertFalse(PasswordPolicy.check(null, "a", "b").hasErrors());
        assertFalse(PasswordPolicy.check("  ", "a", "b").hasErrors());
    }
}
