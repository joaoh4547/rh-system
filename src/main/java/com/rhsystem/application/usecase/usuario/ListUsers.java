package com.rhsystem.application.usecase.usuario;

import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.repository.UserRepository;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case: list users.
 *
 * <p>Provides both full loading ({@link #execute()}) and paginated loading
 * ({@link #execute(int, int)}) for use in server-side grids.
 */
@Service
public class ListUsers {

    private final UserRepository userRepository;

    public ListUsers(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Returns all users (for internal use / reports). */
    @Transactional(readOnly = true)
    public List<User> execute() {
        return userRepository.findAll();
    }

    /**
     * Returns a page of users as a {@link Stream}.
     * The stream must be consumed within the active transaction.
     *
     * @param offset start position (0-based)
     * @param limit  maximum number of records
     */
    @Transactional(readOnly = true)
    public Stream<User> execute(int offset, int limit) {
        return userRepository.findPaginated(offset, limit).stream();
    }
}
