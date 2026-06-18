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

/** Use case: redefine a senha a partir de um token de redefinição válido. */
@Service
public class RedefinirSenha {

    private final TokenAtivacaoRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public RedefinirSenha(TokenAtivacaoRepository tokenRepository,
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
                .orElseThrow(() -> new RegraNegocioException("Token de redefinição inválido."));
        if (token.getFinalidade() != FinalidadeToken.REDEFINICAO_SENHA || !token.isValido()) {
            throw new RegraNegocioException("Token de redefinição expirado ou já utilizado.");
        }

        Usuario usuario = token.getUsuario();
        usuario.redefinirSenha(passwordEncoder.encode(cmd.senha()));
        usuario.setAtualizadoEm(LocalDateTime.now());
        usuarioRepository.salvar(usuario);

        token.setUsado(true);
        tokenRepository.salvar(token);
    }
}
