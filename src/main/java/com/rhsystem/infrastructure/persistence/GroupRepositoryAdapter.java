package com.rhsystem.infrastructure.persistence;

import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.domain.repository.GroupRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
@AllArgsConstructor
public class GroupRepositoryAdapter implements GroupRepository {

    private final JpaGroupRepository jpa;

    @Override
    public Collection<Group> findAllPaginated(int limit, int offset) {
        int page = limit > 0 ? offset / limit : 0;
        return jpa.findAll(PageRequest.of(page, limit)).getContent();
    }
}
