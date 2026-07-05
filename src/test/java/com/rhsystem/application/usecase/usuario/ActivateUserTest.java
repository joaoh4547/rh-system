package com.rhsystem.application.usecase.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

class ActivateUserTest {

    private static ValidatorFactory factory;
    private static CommandValidator commandValidator;

    private ActivationTokenRepository tokenRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private ActivateUser useCase;

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
        useCase = new ActivateUser(tokenRepository, userRepository, passwordEncoder,
                commandValidator);
    }

    private static ActivationToken token(TokenPurpose purpose, LocalDateTime expiresAt) {
        User user = new User();
        user.setStatus(UserStatus.PENDING_CONFIRMATION);
        return new ActivationToken(user, expiresAt, purpose);
    }

    @Test
    void activatesUserEncodingPasswordAndConsumesToken() {
        ActivationToken token = token(TokenPurpose.ACTIVATION, LocalDateTime.now().plusHours(1));
        when(tokenRepository.findByToken(token.getToken())).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("secret1")).thenReturn("HASH");

        useCase.execute(new ActivationCommand(token.getToken(), "secret1", "secret1"));

        User user = token.getUser();
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals("HASH", user.getPassword());
        assertNotNull(user.getUpdatedAt());
        assertTrue(token.isUsed());
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    @Test
    void unknownTokenThrowsInvalid() {
        when(tokenRepository.findByToken("nope")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> useCase.execute(new ActivationCommand("nope", "secret1", "secret1")));
        assertEquals("error.token.activation.invalid", ex.getMessage());
    }

    @Test
    void passwordResetTokenCannotActivateAccount() {
        ActivationToken token = token(TokenPurpose.PASSWORD_RESET, LocalDateTime.now().plusHours(1));
        when(tokenRepository.findByToken(token.getToken())).thenReturn(Optional.of(token));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> useCase.execute(new ActivationCommand(token.getToken(), "secret1", "secret1")));
        assertEquals("error.token.activation.expired", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void expiredTokenIsRejected() {
        ActivationToken token = token(TokenPurpose.ACTIVATION, LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByToken(token.getToken())).thenReturn(Optional.of(token));

        assertThrows(BusinessException.class,
                () -> useCase.execute(new ActivationCommand(token.getToken(), "secret1", "secret1")));
        assertEquals(UserStatus.PENDING_CONFIRMATION, token.getUser().getStatus());
    }

    @Test
    void alreadyUsedTokenIsRejected() {
        ActivationToken token = token(TokenPurpose.ACTIVATION, LocalDateTime.now().plusHours(1));
        token.setUsed(true);
        when(tokenRepository.findByToken(token.getToken())).thenReturn(Optional.of(token));

        assertThrows(BusinessException.class,
                () -> useCase.execute(new ActivationCommand(token.getToken(), "secret1", "secret1")));
    }

    @Test
    void invalidPasswordFailsBeforeTouchingRepositories() {
        assertThrows(ValidationException.class,
                () -> useCase.execute(new ActivationCommand("t", "123", "456")));
        verify(tokenRepository, never()).findByToken(any());
    }
}
