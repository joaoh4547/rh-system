package com.rhsystem.domain.repository;

import com.rhsystem.domain.model.Sorting;
import com.rhsystem.domain.model.parameters.Parameter;

import java.util.Collection;

public interface ParameterRepository {

    Collection<Parameter> findAllPaginated(int limit, int offset,Collection<Sorting> sorting);

    Long count();
}
