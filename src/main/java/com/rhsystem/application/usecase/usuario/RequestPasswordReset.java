package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.port.DomainEventPublisher;
import com.rhsystem.domain.event.PasswordResetRequested;
import com.rhsystem.domain.model.usuario.ActivationToken;
import com.rhsystem.domain.model.usuario.TokenPurpose;
import com.rhsystem.domain.repository.ActivationTokenRepository;
import com.rhsystem.domain.repository.UserRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case: requests a password reset (generates token and publishes
 * {@link PasswordResetRequested}; the email goes out after the commit).
 * Does not reveal whether the email exists, for security.
 */
@Service
public class RequestPasswordReset {

    private final UserRepository userRepository;
    private final ActivationTokenRepository tokenRepository;
    private final DomainEventPublisher events;
    private final long tokenValidityMinutes;

    public RequestPasswordReset(UserRepository userRepository,
                                ActivationTokenRepository tokenRepository,
                                DomainEventPublisher events,
                                @Value("${rh-system.password-reset-token-validity-minutes:30}") long tokenValidityMinutes) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.events = events;
        this.tokenValidityMinutes = tokenValidityMinutes;
    }

    @Transactional
    public void execute(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        userRepository.findByEmail(email.trim()).ifPresent(user -> {
            // Only the newest link works: earlier, still-valid reset tokens are burned.
            tokenRepository.invalidateActiveTokens(user, TokenPurpose.PASSWORD_RESET);
            ActivationToken token = new ActivationToken(user,
                    LocalDateTime.now().plusMinutes(tokenValidityMinutes), TokenPurpose.PASSWORD_RESET);
            tokenRepository.save(token);
            events.publish(PasswordResetRequested.of(user, token.getRawToken()));
        });
    }
}
