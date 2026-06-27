package com.rhsystem.infrastructure.persistence;

import com.google.common.collect.Collections2;
import com.rhsystem.domain.model.Sorting;
import com.rhsystem.domain.model.usuario.UserStatus;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Adapter that implements the domain port by delegating to Spring Data.
 */
@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpa;

    public UserRepositoryAdapter(JpaUserRepository jpa) {
        this.jpa = jpa;
    }

    @Override
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
    public List<User> findAll() {
        return jpa.findAll();
    }

    @Override
    public List<User> findPaginated(int offset, int limit, Collection<Sorting> sorting) {
        int page = limit > 0 ? offset / limit : 0;
        Sort sort = JpaSortUtil.createSort(sorting, Sort.by("firstName").ascending());
        var pageable = PageRequest.of(page, limit, sort);
        return jpa.findAll(pageable).getContent();
    }


    @Override
    public int count() {
        return (int) jpa.count();
    }

    @Override
    public int countByStatus(UserStatus status) {
        return (int) jpa.countByStatus(status);
    }

    @Override
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
