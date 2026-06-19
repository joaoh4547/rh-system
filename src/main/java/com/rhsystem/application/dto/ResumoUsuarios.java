package com.rhsystem.application.dto;

/**
 * Resumo estatístico de usuários — retornado pelo use case
 * {@link com.rhsystem.application.usecase.usuario.BuscarResumoUsuarios}.
 *
 * <p>Calculado diretamente no banco via contagens por status, evitando
 * carregar toda a lista em memória apenas para exibir KPIs.
 */
public record ResumoUsuarios(
        long total,
        long ativos,
        long pendentes,
        long bloqueados
) {}
