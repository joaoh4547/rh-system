package com.rhsystem.infrastructure.persistence;

import com.rhsystem.domain.model.usuario.ActivationToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaActivationTokenRepository extends JpaRepository<ActivationToken, Long> {
    Optional<ActivationToken> findByToken(String token);
}
