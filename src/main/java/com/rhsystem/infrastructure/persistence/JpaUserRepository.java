package com.rhsystem.infrastructure.persistence;

import com.rhsystem.domain.model.usuario.UserStatus;
import com.rhsystem.domain.model.usuario.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for User (infrastructure detail).
 */
public interface JpaUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    /** Loads the user with the {@code groups} collection fetched in the same query. */
    @EntityGraph(attributePaths = "groups")
    Optional<User> findWithGroupsById(Long id);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByCpf(String cpf);

    boolean existsByRg(String rg);

    /** Server-side pagination — Spring Data generates the SQL automatically. */
    Page<User> findAll(Pageable pageable);

    /** Count by status — used for KPIs without loading records. */
    long countByStatus(UserStatus status);
}
