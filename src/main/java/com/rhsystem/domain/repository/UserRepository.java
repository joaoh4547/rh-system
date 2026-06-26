package com.rhsystem.domain.repository;

import com.rhsystem.domain.model.usuario.UserStatus;
import com.rhsystem.domain.model.usuario.User;
import java.util.List;
import java.util.Optional;

/**
 * Persistence port (DDD) for the User aggregate.
 * Implemented in the infrastructure layer.
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findAll();

    /**
     * Returns a page of users ordered by first name.
     *
     * @param offset start position (0-based)
     * @param limit  maximum number of records
     */
    List<User> findPaginated(int offset, int limit);

    /** Total number of registered users. */
    int count();

    /** Total number of users with a given status. */
    int countByStatus(UserStatus status);

    void delete(User user);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    boolean existsByRg(String rg);
}
