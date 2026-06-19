package com.rhsystem.infrastructure.persistence;

import com.rhsystem.domain.model.usuario.StatusUsuario;
import com.rhsystem.domain.model.usuario.Usuario;
import com.rhsystem.domain.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public Optional<Usuario> buscarPorUsername(String username) {
        return jpa.findByUsername(username);
    }

    @Override
    public List<Usuario> listarTodos() {
        return jpa.findAll();
    }

    @Override
    public List<Usuario> listarPaginado(int offset, int limite) {
        int pagina = limite > 0 ? offset / limite : 0;
        var pageable = PageRequest.of(pagina, limite, Sort.by("nomeCompleto").ascending());
        return jpa.findAll(pageable).getContent();
    }

    @Override
    public int contar() {
        return (int) jpa.count();
    }

    @Override
    public int contarPorStatus(StatusUsuario status) {
        return (int) jpa.countByStatus(status);
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
