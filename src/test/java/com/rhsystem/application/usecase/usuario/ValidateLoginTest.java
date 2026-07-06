package com.rhsystem.application.usecase.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.rhsystem.application.dto.login.LoginResult;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.model.usuario.UserStatus;
import com.rhsystem.domain.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class ValidateLoginTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private ValidateLogin useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        useCase = new ValidateLogin(userRepository, passwordEncoder);
    }

    private User user(UserStatus status, String passwordHash, boolean termsAccepted) {
        User user = new User();
        user.setUsername("joao.silva");
        user.setStatus(status);
        user.setPassword(passwordHash);
        if (termsAccepted) {
            user.acceptTerms();
        }
        when(userRepository.findByUsername("joao.silva")).thenReturn(Optional.of(user));
        return user;
    }

    @Test
    void okWhenActiveWithMatchingPasswordAndAcceptedTerms() {
        user(UserStatus.ACTIVE, "hash", true);
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);

        assertEquals(LoginResult.OK, useCase.execute("joao.silva", "secret"));
    }

    @Test
    void termsPendingWhenValidCredentialsButTermsNotAccepted() {
        user(UserStatus.ACTIVE, "hash", false);
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);

        assertEquals(LoginResult.TERMS_PENDING, useCase.execute("joao.silva", "secret"));
    }

    @Test
    void unknownUsernameIsInvalidCredentials() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertEquals(LoginResult.INVALID_CREDENTIALS, useCase.execute("ghost", "x"));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void wrongPasswordIsInvalidCredentials() {
        user(UserStatus.ACTIVE, "hash", true);
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertEquals(LoginResult.INVALID_CREDENTIALS, useCase.execute("joao.silva", "wrong"));
    }

    @Test
    void nonActiveStatusesAreInvalidCredentialsEvenWithRightPassword() {
        for (UserStatus status : new UserStatus[]{UserStatus.INACTIVE, UserStatus.BLOCKED,
                UserStatus.PENDING_CONFIRMATION}) {
            user(status, "hash", true);
            assertEquals(LoginResult.INVALID_CREDENTIALS,
                    useCase.execute("joao.silva", "secret"), "status: " + status);
        }
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void userWithoutPasswordIsInvalidCredentials() {
        user(UserStatus.ACTIVE, null, true);

        assertEquals(LoginResult.INVALID_CREDENTIALS, useCase.execute("joao.silva", "secret"));
        verifyNoInteractions(passwordEncoder);
    }
}
