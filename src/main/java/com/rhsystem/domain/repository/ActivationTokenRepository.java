package com.rhsystem.domain.repository;

import com.rhsystem.domain.model.usuario.ActivationToken;
import com.rhsystem.domain.model.usuario.TokenPurpose;
import com.rhsystem.domain.model.usuario.User;
import java.util.Optional;

/**
 * Persistence port (DDD) for activation tokens.
 */
public interface ActivationTokenRepository {

    ActivationToken save(ActivationToken token);

    /**
     * Finds a token by the <b>raw</b> value received in the email link; the
     * implementation compares its SHA-256 hash ({@link ActivationToken#hash(String)}).
     */
    Optional<ActivationToken> findByToken(String rawToken);

    /**
     * Marks every still-unused token of the user with the given purpose as used, so
     * only the newest link works (avoids piling up valid reset tokens).
     *
     * @return how many tokens were invalidated
     */
    int invalidateActiveTokens(User user, TokenPurpose purpose);
}
