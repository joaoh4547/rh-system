package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.port.NotificadorUsuario;
import com.rhsystem.domain.model.usuario.FinalidadeToken;
import com.rhsystem.domain.model.usuario.TokenAtivacao;
import com.rhsystem.domain.repository.TokenAtivacaoRepository;
import com.rhsystem.domain.repository.UsuarioRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case: solicita redefinição de senha (gera token e envia email).
 * Por segurança, não revela se o email existe.
 */
@Service
public class SolicitarRedefinicaoSenha {

    private final UsuarioRepository usuarioRepository;
    private final TokenAtivacaoRepository tokenRepository;
    private final NotificadorUsuario notificador;
    private final long validadeTokenHoras;

    public SolicitarRedefinicaoSenha(UsuarioRepository usuarioRepository,
                                     TokenAtivacaoRepository tokenRepository,
                                     NotificadorUsuario notificador,
                                     @Value("${rh-system.ativacao-token-validade-horas:24}") long validadeTokenHoras) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.notificador = notificador;
        this.validadeTokenHoras = validadeTokenHoras;
    }

    @Transactional
    public void executar(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        usuarioRepository.buscarPorEmail(email.trim()).ifPresent(usuario -> {
            TokenAtivacao token = new TokenAtivacao(usuario,
                    LocalDateTime.now().plusHours(validadeTokenHoras), FinalidadeToken.REDEFINICAO_SENHA);
            tokenRepository.salvar(token);
            notificador.enviarRedefinicaoSenha(usuario, token.getToken());
        });
    }
}
