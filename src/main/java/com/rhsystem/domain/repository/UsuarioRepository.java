package com.rhsystem.domain.repository;

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

    void remover(Usuario usuario);

    boolean existePorUsername(String username);

    boolean existePorEmail(String email);

    boolean existePorCpf(String cpf);

    boolean existePorRg(String rg);
}
