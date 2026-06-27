package com.rhsystem.domain.repository;

import com.rhsystem.domain.model.grupo.Group;

import java.util.Collection;

public interface GroupRepository {

    Collection<Group> findAllPaginated(int limit, int offset);

    long count();
    long countActive();
}
