package com.rhsystem.infrastructure.persistence;

import com.rhsystem.domain.model.Sorting;
import com.rhsystem.domain.model.parameters.AppParameter;
import com.rhsystem.domain.model.parameters.Parameter;
import com.rhsystem.domain.repository.ParameterRepository;
import com.rhsystem.infrastructure.config.CacheConfig;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class ParameterRepositoryAdapter implements ParameterRepository {

    private final JpaParameterRepository jpa;

    @Override
    @Cacheable(cacheNames = CacheConfig.PARAMETERS, key = "'page:' + #limit + ':' + #offset + ':' + #sorting.toString()")
    public Collection<Parameter> findAllPaginated(int limit, int offset, Collection<Sorting> sorting) {
        int page = limit > 0 ? offset / limit : 0;
        var pageable = JpaSortUtil.createSort(sorting, Sort.by("name"));
        return jpa.findAll(PageRequest.of(page, limit, pageable)).getContent();
    }

    @Override
    public Long count() {
        return jpa.count();
    }

    @Override
    @CacheEvict(cacheNames = CacheConfig.PARAMETERS, allEntries = true)
    public Parameter save(Parameter parameter) {
        return jpa.save(parameter);
    }

    @Override
    public Optional<Parameter> findByParameter(AppParameter parameter) {
        return jpa.findByParameter(parameter);
    }
}
