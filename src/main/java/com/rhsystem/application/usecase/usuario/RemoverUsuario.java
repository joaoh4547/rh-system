package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.exception.RegraNegocioException;
import com.rhsystem.domain.model.usuario.Usuario;
import com.rhsystem.domain.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use case: remove um usuário. */
@Service
public class RemoverUsuario {

    private final UsuarioRepository usuarioRepository;

    public RemoverUsuario(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void executar(Long id) {
        Usuario usuario = usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));
        usuarioRepository.remover(usuario);
    }
}
