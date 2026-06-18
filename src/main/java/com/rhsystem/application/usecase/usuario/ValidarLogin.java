package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.dto.ResultadoLogin;
import com.rhsystem.domain.model.usuario.StatusUsuario;
import com.rhsystem.domain.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case: valida credenciais SEM efetivar a autenticação (usado no login
 * para decidir se exibe o aceite de termos antes de autenticar).
 */
@Service
public class ValidarLogin {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ValidarLogin(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public ResultadoLogin executar(String username, String senha) {
        return usuarioRepository.buscarPorUsername(username)
                .filter(u -> u.getStatus() == StatusUsuario.ATIVO)
                .filter(u -> u.getSenha() != null && passwordEncoder.matches(senha, u.getSenha()))
                .map(u -> u.termosAceitos() ? ResultadoLogin.OK : ResultadoLogin.TERMOS_PENDENTES)
                .orElse(ResultadoLogin.CREDENCIAIS_INVALIDAS);
    }
}
