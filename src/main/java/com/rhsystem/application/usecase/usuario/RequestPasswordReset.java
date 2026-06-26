package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.port.UserNotifier;
import com.rhsystem.domain.model.usuario.ActivationToken;
import com.rhsystem.domain.model.usuario.TokenPurpose;
import com.rhsystem.domain.repository.ActivationTokenRepository;
import com.rhsystem.domain.repository.UserRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case: requests a password reset (generates token and sends email).
 * Does not reveal whether the email exists, for security.
 */
@Service
public class RequestPasswordReset {

    private final UserRepository userRepository;
    private final ActivationTokenRepository tokenRepository;
    private final UserNotifier notifier;
    private final long tokenValidityHours;

    public RequestPasswordReset(UserRepository userRepository,
                                ActivationTokenRepository tokenRepository,
                                UserNotifier notifier,
                                @Value("${rh-system.ativacao-token-validade-horas:24}") long tokenValidityHours) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.notifier = notifier;
        this.tokenValidityHours = tokenValidityHours;
    }

    @Transactional
    public void execute(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        userRepository.findByEmail(email.trim()).ifPresent(user -> {
            ActivationToken token = new ActivationToken(user,
                    LocalDateTime.now().plusHours(tokenValidityHours), TokenPurpose.PASSWORD_RESET);
            tokenRepository.save(token);
            notifier.sendPasswordReset(user, token.getToken());
        });
    }
}
