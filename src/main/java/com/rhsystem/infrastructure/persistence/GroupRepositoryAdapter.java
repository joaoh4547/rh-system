package com.rhsystem.infrastructure.persistence;

import com.rhsystem.domain.model.Sorting;
import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.domain.repository.GroupRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
@AllArgsConstructor
public class GroupRepositoryAdapter implements GroupRepository {

    private final JpaGroupRepository jpa;

    @Override
    public Collection<Group> findAllPaginated(int limit, int offset, Collection<Sorting> sorting) {
        int page = limit > 0 ? offset / limit : 0;
        var pageable = JpaSortUtil.createSort(sorting, Sort.by("name"));
        return jpa.findAll(PageRequest.of(page, limit, pageable)).getContent();
    }

    @Override
    public long count() {
        return jpa.count();
    }

    @Override
    public long countActive() {
        return jpa.countByActiveTrue();
    }
}
