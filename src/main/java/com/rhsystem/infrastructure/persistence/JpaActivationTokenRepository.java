package com.rhsystem.infrastructure.persistence;

import com.rhsystem.domain.model.usuario.ActivationToken;
import com.rhsystem.domain.model.usuario.TokenPurpose;
import com.rhsystem.domain.model.usuario.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaActivationTokenRepository extends JpaRepository<ActivationToken, Long> {

    Optional<ActivationToken> findByTokenHash(String tokenHash);

    @Modifying(flushAutomatically = true)
    @Query("update ActivationToken t set t.used = true "
            + "where t.user = :user and t.purpose = :purpose and t.used = false")
    int invalidateActive(@Param("user") User user, @Param("purpose") TokenPurpose purpose);
}
