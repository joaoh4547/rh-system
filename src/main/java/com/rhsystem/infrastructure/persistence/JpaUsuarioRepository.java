package com.rhsystem.infrastructure.persistence;

import com.rhsystem.domain.model.usuario.StatusUsuario;
import com.rhsystem.domain.model.usuario.Usuario;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório Spring Data do Usuario (detalhe de infraestrutura).
 */
public interface JpaUsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByCpf(String cpf);

    boolean existsByRg(String rg);

    /** Paginação server-side — Spring Data gera o SQL automaticamente. */
    Page<Usuario> findAll(Pageable pageable);

    /** Contagem por status — usada nos KPIs sem carregar registros. */
    long countByStatus(StatusUsuario status);
}
