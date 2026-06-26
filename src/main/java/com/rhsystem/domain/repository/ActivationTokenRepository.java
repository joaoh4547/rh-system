package com.rhsystem.domain.repository;

import com.rhsystem.domain.model.usuario.ActivationToken;
import java.util.Optional;

/**
 * Persistence port (DDD) for activation tokens.
 */
public interface ActivationTokenRepository {

    ActivationToken save(ActivationToken token);

    Optional<ActivationToken> findByToken(String token);
}
