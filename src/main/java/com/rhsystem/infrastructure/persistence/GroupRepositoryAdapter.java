package com.rhsystem.infrastructure.persistence;

import com.rhsystem.domain.model.Sorting;
import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.domain.repository.GroupRepository;
import com.rhsystem.infrastructure.config.CacheConfig;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * Adapter for the Group port. Read queries are cached in the distributed cache
 * ({@link CacheConfig#GROUPS}); entries expire by TTL. When write operations are
 * added to this port, annotate them with
 * {@code @CacheEvict(cacheNames = CacheConfig.GROUPS, allEntries = true)}.
 */
@Repository
@AllArgsConstructor
public class GroupRepositoryAdapter implements GroupRepository {

    private final JpaGroupRepository jpa;

    @Override
    @Cacheable(cacheNames = CacheConfig.GROUPS,
            key = "'page:' + #limit + ':' + #offset + ':' + #sorting")
    public Collection<Group> findAllPaginated(int limit, int offset, Collection<Sorting> sorting) {
        int page = limit > 0 ? offset / limit : 0;
        var pageable = JpaSortUtil.createSort(sorting, Sort.by("name"));
        return jpa.findAll(PageRequest.of(page, limit, pageable)).getContent();
    }

    @Override
    @Cacheable(cacheNames = CacheConfig.GROUPS, key = "'count'")
    public long count() {
        return jpa.count();
    }

    @Override
    @Cacheable(cacheNames = CacheConfig.GROUPS, key = "'countActive'")
    public long countActive() {
        return jpa.countByActiveTrue();
    }
}
