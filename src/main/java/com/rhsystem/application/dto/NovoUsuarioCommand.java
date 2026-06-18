package com.rhsystem.application.dto;

import java.util.List;

/**
 * Comando para criação de um novo usuário.
 * A senha não é informada aqui — será definida na ativação por email.
 */
public record NovoUsuarioCommand(
        String nome,
        String sobrenome,
        String email,
        String cpf,
        String rg,
        EnderecoDTO endereco,
        List<DocumentoUpload> documentos
) {
}
