package com.rhsystem.application.usecase.usuario;

import com.rhsystem.domain.model.usuario.Usuario;
import com.rhsystem.domain.repository.UsuarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use case: listar todos os usuários. */
@Service
public class ListarUsuarios {

    private final UsuarioRepository usuarioRepository;

    public ListarUsuarios(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Usuario> executar() {
        return usuarioRepository.listarTodos();
    }
}
