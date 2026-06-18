package com.rhsystem.domain.repository;

import com.rhsystem.domain.model.usuario.TokenAtivacao;
import java.util.Optional;

/**
 * Porta de persistência (DDD) dos tokens de ativação.
 */
public interface TokenAtivacaoRepository {

    TokenAtivacao salvar(TokenAtivacao token);

    Optional<TokenAtivacao> buscarPorToken(String token);
}
