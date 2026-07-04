package com.rhsystem.domain.repository;

import com.rhsystem.domain.model.Sorting;
import com.rhsystem.domain.model.grupo.Group;

import java.util.Collection;
import java.util.Optional;

public interface GroupRepository {

    Collection<Group> findAllPaginated(int limit, int offset,Collection<Sorting> sorting);

    long count();
    long countActive();

    Group save(Group group);

    Optional<Group> findById(Long id);

    /**
     * Loads the group with its {@code functionalities} collection initialized.
     * Use when the caller needs the full aggregate outside an open persistence session.
     */
    Optional<Group> findByIdWithFunctionalities(Long id);
}
