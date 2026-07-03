package com.rhsystem.domain.repository;

import com.rhsystem.domain.model.Sorting;
import com.rhsystem.domain.model.grupo.Group;

import java.util.Collection;

public interface GroupRepository {

    Collection<Group> findAllPaginated(int limit, int offset,Collection<Sorting> sorting);

    long count();
    long countActive();

    Group save(Group group);
}
