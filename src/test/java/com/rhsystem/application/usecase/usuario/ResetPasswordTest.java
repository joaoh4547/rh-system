package com.rhsystem.application.usecase.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rhsystem.application.dto.usuario.ActivationCommand;
import com.rhsystem.application.exception.BusinessException;
import com.rhsystem.application.validation.CommandValidator;
import com.rhsystem.domain.model.usuario.ActivationToken;
import com.rhsystem.domain.model.usuario.TokenPurpose;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.model.usuario.UserStatus;
import com.rhsystem.domain.repository.ActivationTokenRepository;
import com.rhsystem.domain.repository.UserRepository;
import com.rhsystem.domain.validation.ValidationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class ResetPasswordTest {

    private static ValidatorFactory factory;
    private static CommandValidator commandValidator;

    private ActivationTokenRepository tokenRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private ResetPassword useCase;

    @BeforeAll
    static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        commandValidator = new CommandValidator(factory.getValidator());
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @BeforeEach
    void setUp() {
        tokenRepository = mock(ActivationTokenRepository.class);
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        useCase = new ResetPassword(tokenRepository, userRepository, passwordEncoder,
                commandValidator);
    }

    @Test
    void resetsPasswordKeepingStatusAndConsumesToken() {
        User user = new User();
        user.setStatus(UserStatus.ACTIVE);
        user.setPassword("old-hash");
        ActivationToken token = new ActivationToken(user,
                LocalDateTime.now().plusHours(1), TokenPurpose.PASSWORD_RESET);
        when(tokenRepository.findByToken(token.getToken())).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newpass1")).thenReturn("NEW-HASH");

        useCase.execute(new ActivationCommand(token.getToken(), "newpass1", "newpass1"));

        assertEquals("NEW-HASH", user.getPassword());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertTrue(token.isUsed());
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    @Test
    void unknownTokenThrowsResetInvalid() {
        when(tokenRepository.findByToken("nope")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> useCase.execute(new ActivationCommand("nope", "newpass1", "newpass1")));
        assertEquals("error.token.reset.invalid", ex.getMessage());
    }

    @Test
    void activationTokenCannotResetPassword() {
        ActivationToken token = new ActivationToken(new User(),
                LocalDateTime.now().plusHours(1), TokenPurpose.ACTIVATION);
        when(tokenRepository.findByToken(token.getToken())).thenReturn(Optional.of(token));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> useCase.execute(new ActivationCommand(token.getToken(), "newpass1", "newpass1")));
        assertEquals("error.token.reset.expired", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void expiredOrUsedTokenIsRejected() {
        ActivationToken expired = new ActivationToken(new User(),
                LocalDateTime.now().minusMinutes(1), TokenPurpose.PASSWORD_RESET);
        when(tokenRepository.findByToken(expired.getToken())).thenReturn(Optional.of(expired));
        assertThrows(BusinessException.class,
                () -> useCase.execute(new ActivationCommand(expired.getToken(), "newpass1", "newpass1")));

        ActivationToken used = new ActivationToken(new User(),
                LocalDateTime.now().plusHours(1), TokenPurpose.PASSWORD_RESET);
        used.setUsed(true);
        when(tokenRepository.findByToken(used.getToken())).thenReturn(Optional.of(used));
        assertThrows(BusinessException.class,
                () -> useCase.execute(new ActivationCommand(used.getToken(), "newpass1", "newpass1")));
    }

    @Test
    void mismatchedPasswordsFailBeforeTouchingRepositories() {
        assertThrows(ValidationException.class,
                () -> useCase.execute(new ActivationCommand("t", "newpass1", "different")));
        verify(tokenRepository, never()).findByToken(any());
    }
}
