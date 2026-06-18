package com.rhsystem.domain.model.usuario;

/**
 * Situação cadastral do usuário.
 */
public enum StatusUsuario {

    ATIVO("Ativo"),
    INATIVO("Inativo"),
    BLOQUEADO("Bloqueado"),
    PENDENTE_CONFIRMACAO("Pendente de confirmação");

    private final String descricao;

    StatusUsuario(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
