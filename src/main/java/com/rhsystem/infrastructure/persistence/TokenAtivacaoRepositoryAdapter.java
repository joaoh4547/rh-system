package com.rhsystem.infrastructure.persistence;

import com.rhsystem.domain.model.usuario.TokenAtivacao;
import com.rhsystem.domain.repository.TokenAtivacaoRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TokenAtivacaoRepositoryAdapter implements TokenAtivacaoRepository {

    private final JpaTokenAtivacaoRepository jpa;

    public TokenAtivacaoRepositoryAdapter(JpaTokenAtivacaoRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public TokenAtivacao salvar(TokenAtivacao token) {
        return jpa.save(token);
    }

    @Override
    public Optional<TokenAtivacao> buscarPorToken(String token) {
        return jpa.findByToken(token);
    }
}
