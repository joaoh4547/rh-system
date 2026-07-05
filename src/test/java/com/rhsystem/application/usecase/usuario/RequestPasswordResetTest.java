package com.rhsystem.application.usecase.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.rhsystem.application.port.UserNotifier;
import com.rhsystem.domain.model.usuario.ActivationToken;
import com.rhsystem.domain.model.usuario.TokenPurpose;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.repository.ActivationTokenRepository;
import com.rhsystem.domain.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RequestPasswordResetTest {

    private UserRepository userRepository;
    private ActivationTokenRepository tokenRepository;
    private UserNotifier notifier;
    private RequestPasswordReset useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        tokenRepository = mock(ActivationTokenRepository.class);
        notifier = mock(UserNotifier.class);
        useCase = new RequestPasswordReset(userRepository, tokenRepository, notifier, 24);
    }

    @Test
    void nullOrBlankEmailIsSilentlyIgnored() {
        useCase.execute(null);
        useCase.execute("   ");
        verifyNoInteractions(userRepository, tokenRepository, notifier);
    }

    @Test
    void unknownEmailDoesNotRevealAnything() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        useCase.execute("ghost@example.com");

        verify(tokenRepository, never()).save(any());
        verify(notifier, never()).sendPasswordReset(any(), anyString());
    }

    @Test
    void knownEmailCreatesResetTokenAndSendsEmail() {
        User user = new User();
        user.setEmail("joao@example.com");
        when(userRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(user));

        useCase.execute("  joao@example.com  "); // email é aparado antes da busca

        ArgumentCaptor<ActivationToken> captor = ArgumentCaptor.forClass(ActivationToken.class);
        verify(tokenRepository).save(captor.capture());
        ActivationToken token = captor.getValue();

        assertEquals(TokenPurpose.PASSWORD_RESET, token.getPurpose());
        assertTrue(token.isValid());
        verify(notifier).sendPasswordReset(user, token.getToken());
    }
}
