package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.dto.AtivacaoCommand;
import com.rhsystem.application.exception.RegraNegocioException;
import com.rhsystem.domain.model.usuario.FinalidadeToken;
import com.rhsystem.domain.model.usuario.TokenAtivacao;
import com.rhsystem.domain.model.usuario.Usuario;
import com.rhsystem.domain.repository.TokenAtivacaoRepository;
import com.rhsystem.domain.repository.UsuarioRepository;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use case: ativa a conta definindo a senha a partir do token de ativação. */
@Service
public class AtivarUsuario {

    private final TokenAtivacaoRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AtivarUsuario(TokenAtivacaoRepository tokenRepository,
                         UsuarioRepository usuarioRepository,
                         PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void executar(AtivacaoCommand cmd) {
        UsuarioSupport.validarSenha(cmd);
        TokenAtivacao token = tokenRepository.buscarPorToken(cmd.token())
                .orElseThrow(() -> new RegraNegocioException("Token de ativação inválido."));
        if (token.getFinalidade() != FinalidadeToken.ATIVACAO || !token.isValido()) {
            throw new RegraNegocioException("Token de ativação expirado ou já utilizado.");
        }

        Usuario usuario = token.getUsuario();
        usuario.ativar(passwordEncoder.encode(cmd.senha()));
        usuario.setAtualizadoEm(LocalDateTime.now());
        usuarioRepository.salvar(usuario);

        token.setUsado(true);
        tokenRepository.salvar(token);
    }
}
