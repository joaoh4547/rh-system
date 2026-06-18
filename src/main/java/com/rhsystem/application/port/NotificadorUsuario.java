package com.rhsystem.application.port;

import com.rhsystem.domain.model.usuario.Usuario;

/**
 * Porta de saída para notificações ao usuário (ex: email de ativação).
 * Implementada na infraestrutura.
 */
public interface NotificadorUsuario {

    /**
     * Envia o email de ativação contendo o link com o token.
     *
     * @param usuario usuário recém-criado
     * @param token   token de ativação a ser embutido no link
     */
    void enviarAtivacao(Usuario usuario, String token);
}
