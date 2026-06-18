package com.rhsystem.infrastructure.persistence;

import com.rhsystem.domain.model.usuario.Usuario;
import com.rhsystem.domain.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Adapter que implementa a porta de domínio delegando ao Spring Data.
 */
@Component
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final JpaUsuarioRepository jpa;

    public UsuarioRepositoryAdapter(JpaUsuarioRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        return jpa.save(usuario);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return jpa.findByEmailIgnoreCase(email);
    }

    @Override
    public List<Usuario> listarTodos() {
        return jpa.findAll();
    }

    @Override
    public void remover(Usuario usuario) {
        jpa.delete(usuario);
    }

    @Override
    public boolean existePorUsername(String username) {
        return jpa.existsByUsername(username);
    }

    @Override
    public boolean existePorEmail(String email) {
        return jpa.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean existePorCpf(String cpf) {
        return jpa.existsByCpf(cpf);
    }

    @Override
    public boolean existePorRg(String rg) {
        return jpa.existsByRg(rg);
    }
}
