package com.rhsystem.infrastructure.persistence;

import com.rhsystem.domain.model.usuario.ActivationToken;
import com.rhsystem.domain.repository.ActivationTokenRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ActivationTokenRepositoryAdapter implements ActivationTokenRepository {

    private final JpaActivationTokenRepository jpa;

    public ActivationTokenRepositoryAdapter(JpaActivationTokenRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ActivationToken save(ActivationToken token) {
        return jpa.save(token);
    }

    @Override
    public Optional<ActivationToken> findByToken(String token) {
        return jpa.findByToken(token);
    }
}
