package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.exception.RegraNegocioException;
import com.rhsystem.domain.model.usuario.Usuario;
import com.rhsystem.domain.repository.UsuarioRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use case: registra o aceite dos termos de uso pelo usuário. */
@Service
public class AceitarTermos {

    private final UsuarioRepository usuarioRepository;

    public AceitarTermos(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void executar(String username) {
        Usuario usuario = usuarioRepository.buscarPorUsername(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));
        usuario.aceitarTermos();
        usuario.setAtualizadoEm(LocalDateTime.now());
        usuarioRepository.salvar(usuario);
    }
}
