package com.rhsystem.application.dto;

import com.rhsystem.domain.model.usuario.StatusUsuario;

/**
 * Comando para atualização de um usuário existente.
 * O username é imutável e a senha é tratada pelo fluxo de ativação.
 */
public record AtualizarUsuarioCommand(
        Long id,
        String nome,
        String sobrenome,
        String email,
        String cpf,
        String rg,
        StatusUsuario status,
        EnderecoDTO endereco
) {
}
