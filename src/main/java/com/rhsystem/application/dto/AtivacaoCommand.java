package com.rhsystem.application.dto;

/**
 * Comando para ativação da conta: token recebido por email + nova senha.
 */
public record AtivacaoCommand(
        String token,
        String senha,
        String confirmacaoSenha
) {
}
