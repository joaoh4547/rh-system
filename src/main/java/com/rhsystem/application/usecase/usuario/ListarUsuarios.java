package com.rhsystem.application.usecase.usuario;

import com.rhsystem.domain.model.usuario.Usuario;
import com.rhsystem.domain.repository.UsuarioRepository;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case: listar usuários.
 *
 * <p>Oferece tanto carregamento completo ({@link #executar()}) quanto
 * paginado ({@link #executar(int, int)}) para uso em grids server-side.
 */
@Service
public class ListarUsuarios {

    private final UsuarioRepository usuarioRepository;

    public ListarUsuarios(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /** Retorna todos os usuários (uso interno / relatórios). */
    @Transactional(readOnly = true)
    public List<Usuario> executar() {
        return usuarioRepository.listarTodos();
    }

    /**
     * Retorna uma página de usuários como {@link Stream}.
     * O stream deve ser consumido dentro da transação ativa.
     *
     * @param offset posição inicial (0-based)
     * @param limite quantidade máxima de registros
     */
    @Transactional(readOnly = true)
    public Stream<Usuario> executar(int offset, int limite) {
        return usuarioRepository.listarPaginado(offset, limite).stream();
    }
}
