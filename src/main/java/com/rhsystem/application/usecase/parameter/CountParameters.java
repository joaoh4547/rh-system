package com.rhsystem.application.usecase.parameter;

import com.rhsystem.domain.model.Functionality.Roles;
import org.springframework.security.access.prepost.PreAuthorize;
import com.rhsystem.domain.repository.ParameterRepository;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@PreAuthorize("hasRole('" + Roles.MANAGE_PARAMETERS + "')")
@AllArgsConstructor
@Service
public class CountParameters {

    private final ParameterRepository repository;

    public long execute(){
        return repository.count();
    }
}
