package com.rhsystem.application.usecase.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.rhsystem.application.port.DomainEventPublisher;
import com.rhsystem.domain.event.PasswordResetRequested;
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
    private DomainEventPublisher events;
    private RequestPasswordReset useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        tokenRepository = mock(ActivationTokenRepository.class);
        events = mock(DomainEventPublisher.class);
        useCase = new RequestPasswordReset(userRepository, tokenRepository, events, 30);
    }

    @Test
    void nullOrBlankEmailIsSilentlyIgnored() {
        useCase.execute(null);
        useCase.execute("   ");
        verifyNoInteractions(userRepository, tokenRepository, events);
    }

    @Test
    void unknownEmailDoesNotRevealAnything() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        useCase.execute("ghost@example.com");

        verify(tokenRepository, never()).save(any());
        verify(events, never()).publish(any());
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
        assertTrue(token.getExpiresAt().isBefore(java.time.LocalDateTime.now().plusMinutes(31)),
                "reset links must be short-lived (30 min)");
        verify(tokenRepository).invalidateActiveTokens(user, TokenPurpose.PASSWORD_RESET);
        ArgumentCaptor<PasswordResetRequested> eventCaptor = ArgumentCaptor.forClass(PasswordResetRequested.class);
        verify(events).publish(eventCaptor.capture());
        assertEquals("joao@example.com", eventCaptor.getValue().email());
        assertEquals(token.getRawToken(), eventCaptor.getValue().resetToken());
    }
}
