package com.rhsystem.domain.repository;

import com.rhsystem.domain.model.usuario.StatusUsuario;
import com.rhsystem.domain.model.usuario.Usuario;
import java.util.List;
import java.util.Optional;

/**
 * Porta de persistência (DDD) do agregado Usuario.
 * Implementada na camada de infraestrutura.
 */
public interface UsuarioRepository {

    Usuario salvar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorEmail(String email);

    Optional<Usuario> buscarPorUsername(String username);

    List<Usuario> listarTodos();

    /**
     * Retorna uma página de usuários ordenada por nome.
     *
     * @param offset posição inicial (0-based)
     * @param limite quantidade máxima de registros
     */
    List<Usuario> listarPaginado(int offset, int limite);

    /** Total de usuários cadastrados. */
    int contar();

    /** Total de usuários em determinado status. */
    int contarPorStatus(StatusUsuario status);

    void remover(Usuario usuario);

    boolean existePorUsername(String username);

    boolean existePorEmail(String email);

    boolean existePorCpf(String cpf);

    boolean existePorRg(String rg);
}
