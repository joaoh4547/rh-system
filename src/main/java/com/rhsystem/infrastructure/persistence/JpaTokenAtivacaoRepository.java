package com.rhsystem.infrastructure.persistence;

import com.rhsystem.domain.model.usuario.TokenAtivacao;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTokenAtivacaoRepository extends JpaRepository<TokenAtivacao, Long> {
    Optional<TokenAtivacao> findByToken(String token);
}
