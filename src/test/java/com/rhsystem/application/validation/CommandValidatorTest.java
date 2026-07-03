package com.rhsystem.application.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rhsystem.application.dto.usuario.ActivationCommand;
import com.rhsystem.application.dto.usuario.CreateUserCommand;
import com.rhsystem.domain.validation.ValidationException;
import com.rhsystem.domain.validation.ValidationResult;
import com.rhsystem.domain.validation.Violation;
import jakarta.validation.Validation;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CommandValidatorTest {

    private static jakarta.validation.ValidatorFactory factory;
    private static CommandValidator commandValidator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        commandValidator = new CommandValidator(factory.getValidator());
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    // ── CreateUserCommand ─────────────────────────────────────────────────────

    @Test
    void validCommandProducesNoViolations() {
        CreateUserCommand cmd = new CreateUserCommand(
                "João", "Silva", "joao@example.com",
                "529.982.247-25", "123456789", null, null);
        assertFalse(commandValidator.check(cmd).hasErrors());
    }

    @Test
    void blankCommandCollectsAllViolationsAtOnce() {
        CreateUserCommand cmd = new CreateUserCommand(
                " ", null, "not-an-email", "111.111.111-11", "", null, null);

        ValidationResult result = commandValidator.check(cmd);
        List<String> keys = result.violations().stream().map(Violation::messageKey).toList();

        assertTrue(keys.contains("error.user.name.required"));   // firstName + lastName
        assertTrue(keys.contains("error.email.invalid"));        // malformed email
        assertTrue(keys.contains("error.cpf.invalid"));          // repeated digits CPF
        assertTrue(keys.contains("error.user.documents.required")); // blank rg
        assertTrue(result.violations().size() >= 4);
    }

    @Test
    void maskedValidCpfIsAccepted() {
        CreateUserCommand cmd = new CreateUserCommand(
                "Ana", "Souza", "ana@example.com",
                "529.982.247-25", "MG1234567", null, null);
        assertFalse(commandValidator.check(cmd).hasErrors());
    }

    // ── ActivationCommand ─────────────────────────────────────────────────────

    @Test
    void shortPasswordAndMismatchAreBothReported() {
        ActivationCommand cmd = new ActivationCommand("token", "123", "456");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> commandValidator.validate(cmd));
        List<String> keys = ex.getViolations().stream().map(Violation::messageKey).toList();

        assertTrue(keys.contains("error.password.too.short"));
        assertTrue(keys.contains("error.password.mismatch"));
    }

    @Test
    void matchingValidPasswordPasses() {
        ActivationCommand cmd = new ActivationCommand("token", "secret1", "secret1");
        assertFalse(commandValidator.check(cmd).hasErrors());
    }

    @Test
    void blankPasswordIsReported() {
        ActivationCommand cmd = new ActivationCommand("token", "", "");
        ValidationResult result = commandValidator.check(cmd);
        List<String> keys = result.violations().stream().map(Violation::messageKey).toList();
        assertTrue(keys.contains("error.password.required"));
    }
}
