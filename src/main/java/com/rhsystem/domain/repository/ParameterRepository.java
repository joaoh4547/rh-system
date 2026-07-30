package com.rhsystem.domain.repository;

import com.rhsystem.domain.model.Sorting;
import com.rhsystem.domain.model.parameters.AppParameter;
import com.rhsystem.domain.model.parameters.Parameter;

import java.util.Collection;
import java.util.Optional;

public interface ParameterRepository {

    Collection<Parameter> findAllPaginated(int limit, int offset,Collection<Sorting> sorting);

    Long count();

    Parameter save(Parameter parameter);

    Optional<Parameter> findByParameter(AppParameter parameter);
}
