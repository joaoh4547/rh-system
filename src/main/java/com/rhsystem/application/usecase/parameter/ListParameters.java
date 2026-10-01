package com.rhsystem.application.usecase.parameter;

import com.rhsystem.domain.model.Functionality.Roles;
import org.springframework.security.access.prepost.PreAuthorize;
import com.rhsystem.domain.model.Sorting;
import com.rhsystem.domain.model.parameters.Parameter;
import com.rhsystem.domain.repository.ParameterRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serial;
import java.util.Collection;
import java.util.stream.Stream;

@PreAuthorize("hasRole('" + Roles.MANAGE_PARAMETERS + "')")
@Service
@AllArgsConstructor
public class ListParameters {

    private final ParameterRepository repository;


    @Transactional(readOnly = true)
    public Stream<Parameter> execute(int limit, int offset, Collection<Sorting> sorting){
        return repository.findAllPaginated(limit, offset, sorting).stream();
    }

}
