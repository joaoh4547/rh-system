package com.rhsystem.infrastructure.persistence;

import com.rhsystem.domain.model.Sorting;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.model.usuario.UserStatus;
import com.rhsystem.domain.repository.UserRepository;
import com.rhsystem.infrastructure.config.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Adapter that implements the domain port by delegating to Spring Data.
 *
 * <p>List/count queries are cached in the distributed cache ({@link CacheConfig#USERS}).
 * Any write ({@code save}/{@code delete}) evicts the whole cache, so all cluster
 * instances see fresh data. Point lookups (by id/username/email) and {@code exists*}
 * checks are intentionally NOT cached: they hit unique indexes (cheap) and must be
 * fresh for authentication and uniqueness validation.</p>
 */
@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpa;

    public UserRepositoryAdapter(JpaUserRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @CacheEvict(cacheNames = CacheConfig.USERS, allEntries = true)
    public User save(User user) {
        return jpa.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmailIgnoreCase(email);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpa.findByUsername(username);
    }

    @Override
    @Cacheable(cacheNames = CacheConfig.USERS, key = "'all'")
    public List<User> findAll() {
        return jpa.findAll();
    }

    @Override
    @Cacheable(cacheNames = CacheConfig.USERS,
            key = "'page:' + #offset + ':' + #limit + ':' + #sorting")
    public List<User> findPaginated(int offset, int limit, Collection<Sorting> sorting) {
        int page = limit > 0 ? offset / limit : 0;
        Sort sort = JpaSortUtil.createSort(sorting, Sort.by("firstName").ascending());
        var pageable = PageRequest.of(page, limit, sort);
        return jpa.findAll(pageable).getContent();
    }


    @Override
    @Cacheable(cacheNames = CacheConfig.USERS, key = "'count'")
    public int count() {
        return (int) jpa.count();
    }

    @Override
    @Cacheable(cacheNames = CacheConfig.USERS, key = "'count:' + #status")
    public int countByStatus(UserStatus status) {
        return (int) jpa.countByStatus(status);
    }

    @Override
    @CacheEvict(cacheNames = CacheConfig.USERS, allEntries = true)
    public void delete(User user) {
        jpa.delete(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpa.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean existsByCpf(String cpf) {
        return jpa.existsByCpf(cpf);
    }

    @Override
    public boolean existsByRg(String rg) {
        return jpa.existsByRg(rg);
    }
}
